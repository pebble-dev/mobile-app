package io.rebble.cobble.shared.domain.calendar

import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.data.calendarWatchappId
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.getDatabase
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.timeline.WatchTimelineSyncer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class CalendarSync(
        scope: CoroutineScope
): AutoCloseable, KoinComponent {
    private val calendarSyncer: PhoneCalendarSyncer by inject()
    private val watchTimelineSyncer: WatchTimelineSyncer by inject()
    private val connectionState: StateFlow<ConnectionState?> by inject(named("connectionState"))

    private val watchConnectedListener = connectionState.filterIsInstance<ConnectionState.Connected>().onEach {
        Logging.d("Watch connected, syncing calendar pins")
        onWatchConnected(it.watch.metadata.filterNotNull().first().isUnfaithful.get() ?: false)
    }.launchIn(scope)

    private suspend fun onWatchConnected(unfaithful: Boolean): Boolean {
        if (unfaithful) {
            Logging.d("Clearing calendar pins from watch")
            return watchTimelineSyncer.clearAllPinsFromWatchAndResync()
        } else {
            return syncTimelineToWatch()
        }
    }

    suspend fun deleteCalendarPinsFromWatch(): Boolean {
        getDatabase().timelinePinDao().setSyncActionForAllPinsFromApp(calendarWatchappId, NextSyncAction.Delete)
        syncTimelineToWatch()
        return true
    }

    suspend fun doFullCalendarSync() {
        calendarSyncer.syncDeviceCalendarsToDb()
        syncTimelineToWatch()
    }

    suspend fun syncTimelineToWatch(): Boolean {
        if (connectionState.value == null) {
            Logging.d("Not syncing timeline to watch, no watch connected")
            return false
        } else {
            Logging.d("Syncing timeline to watch")
            return watchTimelineSyncer.syncPinDatabaseWithWatch()
        }
    }

    override fun close() {
        watchConnectedListener.cancel()
    }
}