package io.rebble.cobble.shared.handlers

import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.libpebblecommon.packets.AppRunStateMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppRunStateHandler(
        private val pebbleDevice: PebbleDevice
) : CobbleHandler {
    init {
        pebbleDevice.negotiationScope.launch {
            val deviceScope = pebbleDevice.connectionScope.filterNotNull().first()
            deviceScope.launch { listenForAppStateChanges() }.invokeOnCompletion {
                pebbleDevice.currentActiveApp.value = null
            }
        }
    }

    private suspend fun listenForAppStateChanges() {
        for (message in pebbleDevice.appRunStateService.receivedMessages) {
            when (message) {
                is AppRunStateMessage.AppRunStateStart -> {
                    pebbleDevice.currentActiveApp.value = message.uuid.get()
                }

                is AppRunStateMessage.AppRunStateStop -> {
                    pebbleDevice.currentActiveApp.value = null
                }

                else -> {
                    error("Unknown message type: $message")
                }
            }
        }
    }
}