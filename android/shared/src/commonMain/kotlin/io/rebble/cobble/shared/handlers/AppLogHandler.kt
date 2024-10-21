package io.rebble.cobble.shared.handlers

import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.libpebblecommon.packets.AppLogReceivedMessage
import io.rebble.libpebblecommon.packets.AppLogShippingControlMessage
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class AppLogHandler(
        private val pebbleDevice: PebbleDevice
): CobbleHandler, KoinComponent {
    init {
        pebbleDevice.negotiationScope.launch {
            val deviceScope = pebbleDevice.connectionScope.filterNotNull().first()
            deviceScope.launch { listenForAppLogs() }
        }
    }

    private suspend fun listenForAppLogs() {
        pebbleDevice.appLogsService.send(AppLogShippingControlMessage(true))
        for (message in pebbleDevice.appLogsService.receivedMessages) {
            if (message is AppLogReceivedMessage) {
                Logging.v("<${message.uuid.get().toString()}> ${message.filename.get()}: ${message.message.get()}")
            }
        }
    }
}