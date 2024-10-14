package io.rebble.cobble.shared.handlers

import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.libpebblecommon.packets.AppRunStateMessage
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppRunStateHandler(
        private val pebbleDevice: PebbleDevice
) : CobbleHandler, KoinComponent {
    private val lockerDao: LockerDao by inject()
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
                    Logging.v("App started: ${message.uuid.get()}")
                    lockerDao.updateLastOpened(message.uuid.get().toString(), Clock.System.now())
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