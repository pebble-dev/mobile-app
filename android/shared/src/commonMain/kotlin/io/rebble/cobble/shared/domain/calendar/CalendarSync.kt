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
import io.rebble.cobble.shared.domain.timeline.WatchTimelineSyncer
import io.rebble.libpebblecommon.packets.WatchVersion
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class CalendarSync(
        scope: CoroutineScope,
        blobDBService: BlobDBService
): AutoCloseable, KoinComponent {
    private val calendarSyncer: PhoneCalendarSyncer by inject()
    private val watchTimelineSyncer = WatchTimelineSyncer(blobDBService)
    private val metadataFlow: Flow<WatchVersion.WatchVersionResponse> by inject(named("connectedWatchMetadata"))
    private val timelinePinDao: TimelinePinDao by inject()
    private val calendarDao: CalendarDao by inject()
    private val calendarEnableChangeFlow: MutableSharedFlow<List<Calendar>> = MutableSharedFlow()

    init {
        Logging.d("CalendarSync init")
    }

    private val watchConnectedListener = metadataFlow.onEach {
        Logging.d("Watch connected, syncing calendar pins")
        val res = onWatchConnected(it.isUnfaithful.get() ?: false)
        Logging.d("Calendar sync result: $res")
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
        watchTimelineSyncer.clearAllPinsFromWatchAndResync()
    }

    private suspend fun syncTimelineToWatch(): Boolean {
        return watchTimelineSyncer.syncPinDatabaseWithWatch()
    }

    override fun close() {
        watchConnectedListener.cancel()
    }
}