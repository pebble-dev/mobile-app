package io.rebble.cobble.shared.middleware

import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.libpebblecommon.packets.AppLogShippingControlMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber

class AppLogController(
        private val pebbleDevice: PebbleDevice,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val logs = ConnectionStateManager.connectionState.flatMapLatest {
        if (it is ConnectionState.Connected) {
            flow {
                toggleAppLogOnWatch(true)

                emitAll(pebbleDevice.appLogsService.receivedMessages.receiveAsFlow())
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
                pebbleDevice.appLogsService.send(AppLogShippingControlMessage(enable))
            }
        } catch (e: Exception) {
            Logging.e("AppLog transmit error", e)
        }
    }
}