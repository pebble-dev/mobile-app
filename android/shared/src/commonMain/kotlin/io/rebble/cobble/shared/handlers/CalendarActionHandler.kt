package io.rebble.cobble.shared.handlers

import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.dao.TimelinePinDao
import io.rebble.cobble.shared.domain.calendar.CalendarAction
import io.rebble.cobble.shared.domain.common.SystemAppIDs.calendarWatchappId
import io.rebble.cobble.shared.domain.timeline.TimelineActionManager
import io.rebble.cobble.shared.domain.timeline.WatchTimelineSyncer
import io.rebble.libpebblecommon.packets.blobdb.TimelineIcon
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import io.rebble.libpebblecommon.services.blobdb.TimelineService
import io.rebble.libpebblecommon.util.TimelineAttributeFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CalendarActionHandler(private val scope: CoroutineScope): KoinComponent, CobbleHandler {
    private val timelineActionManager: TimelineActionManager by inject()
    private val timelinePinDao: TimelinePinDao by inject()
    private val timelineSyncer: WatchTimelineSyncer by inject()

    private val calendarActionFlow = timelineActionManager.actionFlowForApp(calendarWatchappId)

    init {
        calendarActionFlow.onEach {
            val (action, deferred) = it
            val itemId = action.itemID.get()
            val response = try {
                when (CalendarAction.fromID(action.actionID.get().toInt())) {
                    CalendarAction.Remove -> handleRemove(itemId)
                    CalendarAction.Mute -> handleMute(itemId)
                    CalendarAction.Accept -> handleAccept(itemId)
                    CalendarAction.Maybe -> handleMaybe(itemId)
                    CalendarAction.Decline -> handleDecline(itemId)
                }
            } catch (e: NoSuchElementException) {
                TimelineService.ActionResponse(
                        success = false
                )
            }
            deferred.complete(response)
        }.launchIn(scope)
    }

    private suspend fun handleRemove(itemId: Uuid): TimelineService.ActionResponse {
        timelinePinDao.setSyncActionForPins(listOf(itemId), NextSyncAction.Delete)
        timelineSyncer.syncPinDatabaseWithWatch()

        return TimelineService.ActionResponse(
                success = true,
                attributes = listOf(
                        TimelineAttributeFactory.icon(TimelineIcon.ResultDeleted),
                        TimelineAttributeFactory.title("Removed from timeline")
                )
        )
    }

    private suspend fun handleMute(itemId: Uuid): TimelineService.ActionResponse {
        //TODO: Handle mute action
        return TimelineService.ActionResponse(
                success = false,
                attributes = listOf(
                        TimelineAttributeFactory.title("TODO")
                )
        )
    }

    private suspend fun handleAccept(itemId: Uuid): TimelineService.ActionResponse {
        // TODO: Handle accept action
        return TimelineService.ActionResponse(
                success = false,
                attributes = listOf(
                        TimelineAttributeFactory.title("TODO")
                )
        )
    }

    private suspend fun handleMaybe(itemId: Uuid): TimelineService.ActionResponse {
        //TODO: Handle maybe action
        return TimelineService.ActionResponse(
                success = false,
                attributes = listOf(
                        TimelineAttributeFactory.title("TODO")
                )
        )
    }

    private suspend fun handleDecline(itemId: Uuid): TimelineService.ActionResponse {
        //TODO: Handle decline action
        return TimelineService.ActionResponse(
                success = false,
                attributes = listOf(
                        TimelineAttributeFactory.title("TODO")
                )
        )
    }
}