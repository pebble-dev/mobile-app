package io.rebble.cobble.bridges.ui

import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.background.CalendarFlutterBridge
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.util.Debouncer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class CalendarControlFlutterBridge @Inject constructor(
        private val connectionLooper: ConnectionLooper,
        private val calendarFlutterBridge: CalendarFlutterBridge,
        private val coroutineScope: CoroutineScope,
        bridgeLifecycleController: BridgeLifecycleController
) : Pigeons.CalendarControl, FlutterBridge {
    private val debouncer = Debouncer(debouncingTimeMs = 5_000L, coroutineScope)

    init {
        bridgeLifecycleController.setupControl(Pigeons.CalendarControl::setup, this)
    }

    override fun requestCalendarSync() {
        if (connectionLooper.connectionState.value is ConnectionState.Disconnected) {
            // No need to do anything. Calendar will be re-synced automatically when service
            // is restarted
            return
        }

        // Use debouncer to ensure user quickly selecting his/hers calendars will not trigger too
        // many sync requests
        debouncer.executeDebouncing {
            calendarFlutterBridge.syncCalendar()
        }
    }

    override fun deleteCalendarPinsFromWatch() {
        coroutineScope.launch {
            calendarFlutterBridge.deleteCalendarPinsFromWatch()
        }
    }
}