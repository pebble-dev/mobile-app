package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ConnectionLooper @Inject constructor(
        private val context: Context,
        private val blueCommon: BlueCommon,
        private val errorHandler: CoroutineExceptionHandler
) {
    val connectionState: StateFlow<ConnectionState> get() = _connectionState
    private val _connectionState: MutableStateFlow<ConnectionState> = MutableStateFlow(
            ConnectionState.Disconnected
    )

    private val coroutineScope: CoroutineScope = GlobalScope + errorHandler

    private var currentConnection: Job? = null
    private var lastConnectedWatch: String? = null

    fun connectToWatch(macAddress: String) {
        coroutineScope.launch {
            try {
                lastConnectedWatch = macAddress

                currentConnection?.cancelAndJoin()
                currentConnection = coroutineContext[Job]

                launchRestartOnBluetoothOff(macAddress)

                var retryTime = HALF_OF_INITAL_RETRY_TIME
                while (isActive) {
                    if (BluetoothAdapter.getDefaultAdapter()?.isEnabled != true) {
                        Timber.d("Bluetooth is off. Waiting until it is on Cancel connection attempt.")

                        _connectionState.value = ConnectionState.WaitingForBluetoothToEnable(
                                BluetoothAdapter.getDefaultAdapter()?.getRemoteDevice(macAddress)
                        )

                        getBluetoothStatus(context).first { bluetoothOn -> bluetoothOn == true }
                    }

                    try {
                        blueCommon.startSingleWatchConnection(macAddress).collect {
                            _connectionState.value = it.toConnectionStatus()
                            if (it is SingleConnectionStatus.Connected) {
                                retryTime = HALF_OF_INITAL_RETRY_TIME
                            }
                        }
                    } catch (_: CancellationException) {
                        // Do nothing. Cancellation is OK
                    } catch (e: Exception) {
                        Timber.e(e, "Watch connection error")
                    }


                    val lastWatch = connectionState.value.watchOrNull

                    retryTime *= 2
                    if (retryTime > MAX_RETRY_TIME) {
                        Timber.d("Watch failed to connect after numerous attempts. Abort connection.")

                        break
                    }
                    Timber.d("Watch connection failed, waiting and reconnecting after $retryTime ms")
                    _connectionState.value = ConnectionState.WaitingForReconnect(lastWatch)
                    delay(retryTime)
                }
            } finally {
                _connectionState.value = ConnectionState.Disconnected
                lastConnectedWatch = null
            }
        }
    }

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
        currentConnection?.cancel()
    }

    /**
     * Get [CoroutineScope] that is active while watch is connected and cancelled if watch
     * disconnects.
     */
    fun getWatchConnectedScope(
            context: CoroutineContext = EmptyCoroutineContext
    ): CoroutineScope {
        val scope = CoroutineScope(SupervisorJob() + errorHandler + context)

        scope.launch(Dispatchers.Unconfined) {
            connectionState.collect {
                if (it !is ConnectionState.Connected) {
                    scope.cancel()
                }
            }
        }

        return scope
    }
}

private fun SingleConnectionStatus.toConnectionStatus(): ConnectionState {
    return when (this) {
        is SingleConnectionStatus.Connecting -> ConnectionState.Connecting(watch)
        is SingleConnectionStatus.Connected -> ConnectionState.Connected(watch)
    }
}

private const val HALF_OF_INITAL_RETRY_TIME = 2_000L // initial retry = 4 seconds
private const val MAX_RETRY_TIME = 10 * 3600 * 1000L // 10 hours