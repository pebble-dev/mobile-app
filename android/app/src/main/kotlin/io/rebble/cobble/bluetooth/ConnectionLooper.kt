package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothAdapter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import io.rebble.cobble.bluetooth.classic.ReconnectionSocketServer
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ConnectionLooper
    @Inject
    constructor(
        private val context: Context,
        private val blueCommon: DeviceTransport,
        private val errorHandler: CoroutineExceptionHandler
    ) {
        val connectionState: StateFlow<ConnectionState> get() = _connectionState
        private val _connectionState: MutableStateFlow<ConnectionState> = /*MutableStateFlow(
            ConnectionState.Disconnected
    )*/ ConnectionStateManager.connectionState
        private val _watchPresenceState = MutableStateFlow<String?>(null)
        val watchPresenceState: StateFlow<String?> get() = _watchPresenceState

        private val coroutineScope: CoroutineScope = GlobalScope + errorHandler

        private var currentConnection: Job? = null
        private var lastConnectedWatch: String? = null
        private var delayJob: Job? = null

        fun signalWatchPresence(macAddress: String) {
            _watchPresenceState.value = macAddress
            if (lastConnectedWatch == macAddress) {
                delayJob?.cancel()
            }
        }

        fun signalWatchAbsence() {
            _watchPresenceState.value = null
        }

        fun tryReconnect() {
            try {
                lastConnectedWatch?.let {
                    connectToWatch(it)
                }
            } catch (e: SecurityException) {
                Timber.e(e, "Failed to trigger reconnect due to permissions issue")
            }
        }

        @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
        fun connectToWatch(macAddress: String) {
            coroutineScope.launch {
                if (currentConnection != null) {
                    Timber.d("ConnectionLooper already targeting watch, will cancel.")
                }
                lastConnectedWatch = macAddress

                try {
                    withTimeout(2000) {
                        currentConnection?.cancelAndJoin()
                    }
                } catch (_: TimeoutCancellationException) {
                    Timber.w("Failed to cancel connection in time")
                }
                currentConnection =
                    launch {
                        try {
                            launchRestartOnBluetoothOff(macAddress)

                            var retryTime = HALF_OF_INITAL_RETRY_TIME
                            var retries = 0
                            val reconnectionSocketServer =
                                ReconnectionSocketServer(BluetoothAdapter.getDefaultAdapter()!!)
                            reconnectionSocketServer.start().onEach {
                                Timber.d("Reconnection socket server received connection from $it")
                                signalWatchPresence(macAddress)
                            }.launchIn(this + CoroutineName("ReconnectionSocketServer"))
                            while (isActive) {
                                if (BluetoothAdapter.getDefaultAdapter()?.isEnabled != true) {
                                    Timber.d("Bluetooth is off. Waiting until it is on Cancel connection attempt.")

                                    _connectionState.value = ConnectionState.WaitingForTransport(macAddress)

                                    getBluetoothStatus(context).first { bluetoothOn -> bluetoothOn }
                                }
                                val connectionScope = CoroutineScope(SupervisorJob() + errorHandler + Dispatchers.IO) + CoroutineName("ConnectionScope-$macAddress")
                                try {
                                    blueCommon.startSingleWatchConnection(macAddress).collect {
                                        if (it is SingleConnectionStatus.Connected /*&& connectionState.value !is ConnectionState.Connected && connectionState.value !is ConnectionState.RecoveryMode*/) {
                                            // initial connection, wait on negotiation
                                            _connectionState.value = ConnectionState.Negotiating(it.watch)
                                        } else {
                                            Timber.d("Not waiting for negotiation")
                                            _connectionState.value = it.toConnectionStatus()
                                        }
                                        if (it is SingleConnectionStatus.Connected) {
                                            retryTime = HALF_OF_INITAL_RETRY_TIME
                                            retries = 0
                                            _connectionState.value.watchOrNull?.connectionScope?.value = connectionScope
                                        }
                                    }
                                } catch (_: CancellationException) {
                                    // Do nothing. Cancellation is OK
                                } catch (e: Exception) {
                                    Timber.e(e, "Watch connection error")
                                } finally {
                                    connectionScope.cancel("Connection ended")
                                }

                                if (isActive) {
                                    val lastWatch = connectionState.value.watchOrNull

                                    retryTime = min(retryTime + HALF_OF_INITAL_RETRY_TIME, MAX_RETRY_TIME)
                                    retries++
                                    Timber.d("Watch connection failed, waiting and reconnecting after $retryTime ms (retry: $retries)")
                                    _connectionState.value = ConnectionState.WaitingForReconnect(lastWatch)
                                    delayJob =
                                        launch(CoroutineName("DelayJob")) {
                                            delay(retryTime)
                                        }
                                    try {
                                        delayJob?.join()
                                    } catch (_: CancellationException) {
                                        Timber.i("Reconnect delay interrupted")
                                        retryTime = HALF_OF_INITAL_RETRY_TIME
                                        retries = 0
                                    }
                                }
                            }
                        } finally {
                            _connectionState.value = ConnectionState.Disconnected
                            lastConnectedWatch = null
                        }
                    }
            }
        }

        @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
        private fun CoroutineScope.launchRestartOnBluetoothOff(macAddress: String) {
            launch {
                var previousState = false
                getBluetoothStatus(context).collect { newState ->
                    if (previousState && !newState) {
                        Timber.d("Bluetooth turned off. Restart connection.")
                        // Re-calling connect will kill this coroutine and restart new one
                        // that will wait until bluetooth is turned back on
                        connectToWatch(macAddress)
                    }

                    previousState = newState
                }
            }
        }

        fun closeConnection() {
            lastConnectedWatch?.let {
                val companionDeviceManager =
                    context.getSystemService(
                        CompanionDeviceManager::class.java
                    )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    companionDeviceManager.stopObservingDevicePresence(it)
                }
            }
            currentConnection?.cancel()
        }
    }

private fun SingleConnectionStatus.toConnectionStatus(): ConnectionState {
    return when (this) {
        is SingleConnectionStatus.Connecting -> ConnectionState.Connecting(watch)
        is SingleConnectionStatus.Connected -> ConnectionState.Connected(watch)
    }
}

private const val HALF_OF_INITAL_RETRY_TIME = 2_000L // initial retry = 4 seconds
private const val MAX_RETRY_TIME = 10_000L // Max retry = 10 seconds