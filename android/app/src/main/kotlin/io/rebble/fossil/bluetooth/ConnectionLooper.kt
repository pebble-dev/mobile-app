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

                while (isActive) {
                    try {
                        blueCommon.startSingleWatchConnection(macAddress).collect {
                            _connectionState.value = it.toConnectionStatus()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Watch connection error", e)
                    }

                    // TODO exponential backoff
                    Log.d(TAG, "Watch connection failed, waiting and reconnecting")
                    delay(5000)
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

private const val TAG = "ConnectionLooper"