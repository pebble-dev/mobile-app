package io.rebble.cobble.shared.domain.calendar

import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.dao.CalendarDao
import io.rebble.cobble.shared.database.dao.TimelinePinDao
import io.rebble.cobble.shared.database.entity.Calendar
import io.rebble.cobble.shared.database.getDatabase
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.domain.common.SystemAppIDs.calendarWatchappId
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.watchOrNull
import io.rebble.cobble.shared.domain.timeline.WatchTimelineSyncer
import io.rebble.libpebblecommon.packets.WatchVersion
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class CalendarSync(
        scope: CoroutineScope
): AutoCloseable, KoinComponent {
    private val calendarSyncer: PhoneCalendarSyncer by inject()
    private val connectionState: StateFlow<ConnectionState> by inject(named("connectionState"))
    private val timelinePinDao: TimelinePinDao by inject()
    private val calendarDao: CalendarDao by inject()
    private val calendarEnableChangeFlow: MutableSharedFlow<List<Calendar>> = MutableSharedFlow()

    init {
        Logging.d("CalendarSync init")
    }

    private val watchConnectedListener = connectionState.filterIsInstance<ConnectionState.Connected>().onEach {
        Logging.d("Watch connected, syncing calendar pins")
        it.watch.connectionScope.value!!.launch {
            val res = onWatchConnected(it.watch.metadata.filterNotNull().first().isUnfaithful.get() ?: false, it.watch)
            Logging.d("Calendar sync result: $res")
        }
    }.launchIn(scope)

    private suspend fun onWatchConnected(unfaithful: Boolean, watch: PebbleDevice): Boolean {
        if (unfaithful) {
            val watchTimelineSyncer = WatchTimelineSyncer(watch.blobDBService)
            Logging.d("Clearing calendar pins from watch")
            return watchTimelineSyncer.clearAllPinsFromWatchAndResync()
        } else {
            return syncTimelineToWatch()
        }
    }

    suspend fun deleteCalendarPinsFromWatch(): Boolean {
        timelinePinDao.setSyncActionForAllPinsFromApp(calendarWatchappId, NextSyncAction.Delete)
        syncTimelineToWatch()
        return true
    }

    suspend fun doFullCalendarSync(): Boolean {
        return if (calendarSyncer.syncDeviceCalendarsToDb()) {
            syncTimelineToWatch()
        } else {
            true
        }
    }

    suspend fun getCalendars(): List<Calendar> {
        return calendarDao.getAll()
    }

    fun getUpdatesFlow(): Flow<List<Calendar>> {
        return calendarDao.getFlow()
    }

    suspend fun setCalendarEnabled(calendarId: Long, enabled: Boolean) {
        calendarDao.setEnabled(calendarId, enabled)
    }

    suspend fun forceFullResync() {
        Logging.i("Forcing full calendar resync")
        calendarSyncer.clearAllCalendarsFromDb()
        calendarSyncer.syncDeviceCalendarsToDb()

        connectionState.value.watchOrNull?.let {
            val connectionScope = it.connectionScope.value
            connectionScope?.async {
                val watchTimelineSyncer = WatchTimelineSyncer(it.blobDBService)
                watchTimelineSyncer.clearAllPinsFromWatchAndResync()
            }?.await()
        }
    }

    private suspend fun syncTimelineToWatch(): Boolean {
        connectionState.value.watchOrNull?.let {
            val connectionScope = it.connectionScope.value
            return connectionScope?.async {
                val watchTimelineSyncer = WatchTimelineSyncer(it.blobDBService)
                return@async watchTimelineSyncer.syncPinDatabaseWithWatch()
            }?.await() ?: false
        } ?: return false
    }

    override fun close() {
        watchConnectedListener.cancel()
    }
}