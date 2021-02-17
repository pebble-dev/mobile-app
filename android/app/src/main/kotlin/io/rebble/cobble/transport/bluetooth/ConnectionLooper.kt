package io.rebble.cobble.transport.bluetooth

import io.rebble.cobble.transport.emulator.EmuSocketDriver
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ConnectionLooper @Inject constructor(
        private val blueCommon: BlueCommon,
        private val emuDriver: EmuSocketDriver,
        errorHandler: CoroutineExceptionHandler
) {
    val connectionState: StateFlow<ConnectionState> get() = _connectionState
    private val _connectionState: MutableStateFlow<ConnectionState> = MutableStateFlow(
            ConnectionState.Disconnected
    )

    private val coroutineScope: CoroutineScope = GlobalScope + errorHandler

    private var currentConnection: Job? = null

    var isEmulator = false

    fun connectToWatch(macAddress: String) {
        coroutineScope.launch {
            try {
                currentConnection?.cancelAndJoin()
                currentConnection = coroutineContext[Job]
                isEmulator = false

                var retryTime = HALF_OF_INITAL_RETRY_TIME
                while (isActive) {
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
            }
        }
    }

    fun connectToEmulator(host: String, port: Int) {
        coroutineScope.launch {
            try {
                currentConnection?.cancelAndJoin()
                currentConnection = coroutineContext[Job]
                isEmulator = true
                var retries = 0
                while (isActive) {
                    try {
                        emuDriver.startEmulatorConnection(host, port).collect {
                            _connectionState.value = it.toConnectionStatus()
                            if (it is SingleConnectionStatus.Connected) {
                                retries = 0
                            }
                        }
                    } catch (_: CancellationException) {

                    } catch (e: Exception) {
                        Timber.e(e, "Emulator connection error")
                    }
                    retries++
                    if (retries > MAX_EMU_RETRIES) {
                        Timber.d("Emulator failed to connect after numerous attempts. Abort connection.")
                        break
                    }
                    delay(2000)
                }
            } finally {
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    fun closeConnection() {
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
private const val MAX_RETRY_TIME = 10 * 3600 * 1000L // 10 hours
private const val MAX_EMU_RETRIES = 5