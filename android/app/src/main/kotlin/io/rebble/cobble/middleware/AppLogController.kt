package io.rebble.cobble.middleware

import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import io.rebble.libpebblecommon.packets.AppLogShippingControlMessage
import io.rebble.libpebblecommon.services.AppLogService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class AppLogController @Inject constructor(
    connectionLooper: ConnectionLooper,
    private val appLogsService: AppLogService
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val logs = connectionLooper.connectionState.flatMapLatest {
        if (it is ConnectionState.Connected) {
            flow {
                toggleAppLogOnWatch(true)

                emitAll(appLogsService.receivedMessages.receiveAsFlow())
            }
        } else {
            emptyFlow()
        }
    }.onCompletion {
        toggleAppLogOnWatch(false)
    }.shareIn(GlobalScope, SharingStarted.WhileSubscribed(1000, 1000))

    private suspend fun toggleAppLogOnWatch(enable: Boolean) {
        try {
            withContext(NonCancellable) {
                appLogsService.send(AppLogShippingControlMessage(enable))
            }
        } catch (e: Exception) {
            Timber.e(e, "AppLog transmit error")
        }
    }
}