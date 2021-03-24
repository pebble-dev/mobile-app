package io.rebble.cobble.bluetooth

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ConnectionLooper @Inject constructor(
        private val blueCommon: BlueCommon,
        private val errorHandler: CoroutineExceptionHandler
) {
    val connectionState: StateFlow<ConnectionState> get() = _connectionState
    private val _connectionState: MutableStateFlow<ConnectionState> = MutableStateFlow(
            ConnectionState.Disconnected
    )

    private val coroutineScope: CoroutineScope = GlobalScope + errorHandler

    private var currentConnection: Job? = null

    fun connectToWatch(macAddress: String) {
        coroutineScope.launch {
            try {
                currentConnection?.cancelAndJoin()
                currentConnection = coroutineContext[Job]

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