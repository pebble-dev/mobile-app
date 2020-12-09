package io.rebble.cobble.handlers

import io.rebble.cobble.bridges.background.CalendarFlutterBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class CalendarHandler @Inject constructor(
        coroutineScope: CoroutineScope,
        private val calendarFlutterBridge: CalendarFlutterBridge
) : PebbleMessageHandler {
    init {
        coroutineScope.launch {
            // We were not receiving any calendar changes when service was offline.
            // Sync calendar at startup

            calendarFlutterBridge.triggerCalendarSync()
        }
    }
}