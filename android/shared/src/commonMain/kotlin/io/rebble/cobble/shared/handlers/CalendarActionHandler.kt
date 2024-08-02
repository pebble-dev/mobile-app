package io.rebble.cobble.shared.handlers

import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.dao.TimelinePinDao
import io.rebble.cobble.shared.domain.calendar.CalendarAction
import io.rebble.cobble.shared.domain.calendar.PlatformCalendarActionExecutor
import io.rebble.cobble.shared.domain.common.SystemAppIDs.calendarWatchappId
import io.rebble.cobble.shared.domain.timeline.TimelineActionManager
import io.rebble.cobble.shared.domain.timeline.WatchTimelineSyncer
import io.rebble.libpebblecommon.packets.blobdb.TimelineIcon
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import io.rebble.libpebblecommon.services.blobdb.TimelineService
import io.rebble.libpebblecommon.util.TimelineAttributeFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CalendarActionHandler(private val scope: CoroutineScope): KoinComponent, CobbleHandler {
    private val timelineActionManager: TimelineActionManager by inject()
    private val timelinePinDao: TimelinePinDao by inject()
    private val timelineSyncer: WatchTimelineSyncer by inject()
    private val calendarActionExecutor: PlatformCalendarActionExecutor by inject()

    private val calendarActionFlow = timelineActionManager.actionFlowForApp(calendarWatchappId)

    init {
        calendarActionFlow.onEach {
            val (action, deferred) = it
            val itemId = action.itemID.get()
            val response = try {
                when (val actionName = CalendarAction.fromID(action.actionID.get().toInt())) {
                    CalendarAction.Remove -> handleRemove(itemId)
                    CalendarAction.Mute -> handleMute(itemId)
                    CalendarAction.Accept, CalendarAction.Maybe, CalendarAction.Decline -> {
                        val pin = timelinePinDao.get(itemId)
                        if (pin != null) {
                            calendarActionExecutor.handlePlatformAction(actionName, pin)
                        } else {
                            Logging.w("Received calendar action for non-existent pin")
                            TimelineService.ActionResponse(
                                    success = false
                            )
                        }
                    }
                }
            } catch (e: NoSuchElementException) {
                TimelineService.ActionResponse(
                        success = false
                )
            }
            deferred.complete(response)
        }.catch {
            Logging.e("Error while handling calendar action", it)
        }.launchIn(scope)
    }

    private suspend fun handleRemove(itemId: Uuid): TimelineService.ActionResponse {
        timelinePinDao.setSyncActionForPins(listOf(itemId), NextSyncAction.Delete)
        timelineSyncer.syncPinDatabaseWithWatch()

        return TimelineService.ActionResponse(
                success = true,
                attributes = listOf(
                        TimelineAttributeFactory.largeIcon(TimelineIcon.ResultDeleted),
                        TimelineAttributeFactory.subtitle("Removed from timeline")
                )
        )
    }

    private suspend fun handleMute(itemId: Uuid): TimelineService.ActionResponse {
        //TODO: Handle mute action
        return TimelineService.ActionResponse(
                success = false,
                attributes = listOf(
                        TimelineAttributeFactory.subtitle("TODO")
                )
        )
    }
}