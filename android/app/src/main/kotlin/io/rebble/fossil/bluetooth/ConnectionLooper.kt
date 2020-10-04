package io.rebble.fossil.bluetooth

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ConnectionLooper @Inject constructor(
        private val blueCommon: BlueCommon,
        errorHandler: CoroutineExceptionHandler
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
                    } catch (e: Exception) {
                        Log.e(TAG, "Watch connection error", e)
                    }


                    val lastWatch = connectionState.value.watchOrNull
                    _connectionState.value = ConnectionState.Connecting(lastWatch)

                    retryTime *= 2
                    if (retryTime > MAX_RETRY_TIME) {
                        Log.d(TAG, "Watch failed to connect after numerous attempts. Abort connection.")

                        break
                    }
                    Log.d(TAG, "Watch connection failed, waiting and reconnecting after $retryTime ms")
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
}

private fun SingleConnectionStatus.toConnectionStatus(): ConnectionState {
    return when (this) {
        is SingleConnectionStatus.Connecting -> ConnectionState.Connecting(watch)
        is SingleConnectionStatus.Connected -> ConnectionState.Connected(watch)
    }
}

private const val HALF_OF_INITAL_RETRY_TIME = 2_000L // initial retry = 4 seconds
private const val MAX_RETRY_TIME = 10 * 3600 * 1000L // 10 hours
private const val TAG = "ConnectionLooper"