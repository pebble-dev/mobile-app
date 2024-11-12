package io.rebble.cobble.shared.domain.calendar

import android.content.ContentValues
import android.provider.CalendarContract
import io.rebble.cobble.shared.AndroidPlatformContext
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.data.toCompositeBackingId
import io.rebble.cobble.shared.database.dao.CalendarDao
import io.rebble.cobble.shared.database.entity.TimelinePin
import io.rebble.cobble.shared.domain.timeline.WatchTimelineSyncer
import io.rebble.libpebblecommon.packets.blobdb.TimelineIcon
import io.rebble.libpebblecommon.services.blobdb.TimelineService
import io.rebble.libpebblecommon.util.TimelineAttributeFactory
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.days

class AndroidCalendarActionExecutor(private val watchTimelineSyncer: WatchTimelineSyncer): PlatformCalendarActionExecutor, KoinComponent {
    private val platformContext: PlatformContext by inject()
    private val calendarDao: CalendarDao by inject()

    override suspend fun handlePlatformAction(action: CalendarAction, pin: TimelinePin): TimelineService.ActionResponse {
        val instanceId = pin.backingId ?: run {
            Logging.e("No backing ID for calendar pin")
            return TimelineService.ActionResponse(success = false)
        }
        val event = getCalendarInstanceById(platformContext, instanceId.toCompositeBackingId().eventId.toString(), Clock.System.now()-1.days, Clock.System.now()+30.days) ?: run {
            Logging.e("No calendar event found for ID $instanceId")
            return TimelineService.ActionResponse(success = false)
        }
        val eventId = event.baseEventId
        val calendar = calendarDao.get(event.calendarId) ?: run {
            Logging.e("No calendar found for ID ${event.calendarId}")
            return TimelineService.ActionResponse(success = false)
        }

        return when (action) {
            CalendarAction.Accept -> {
                if (updateAttendeeStatus(eventId.toString(), CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED, calendar.ownerId)) {
                    TimelineService.ActionResponse(
                            success = true,
                            attributes = listOf(
                                    TimelineAttributeFactory.subtitle("Accepted"),
                                    TimelineAttributeFactory.largeIcon(TimelineIcon.ResultSent),
                            )
                    )
                } else {
                    TimelineService.ActionResponse(success = false)
                }
            }
            CalendarAction.Maybe -> {
                if (updateAttendeeStatus(eventId.toString(), CalendarContract.Attendees.ATTENDEE_STATUS_TENTATIVE, calendar.ownerId)) {
                    TimelineService.ActionResponse(
                            success = true,
                            attributes = listOf(
                                    TimelineAttributeFactory.subtitle("Sent Maybe"),
                                    TimelineAttributeFactory.largeIcon(TimelineIcon.ResultSent),
                            )
                    )
                } else {
                    TimelineService.ActionResponse(success = false)
                }
            }
            CalendarAction.Decline -> {
                if (updateAttendeeStatus(eventId.toString(), CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED, calendar.ownerId)) {
                    TimelineService.ActionResponse(
                            success = true,
                            attributes = listOf(
                                    TimelineAttributeFactory.subtitle("Declined"),
                                    TimelineAttributeFactory.largeIcon(TimelineIcon.ResultSent),
                            )
                    )
                } else {
                    TimelineService.ActionResponse(success = false)
                }
            }
            CalendarAction.Remove -> {
                watchTimelineSyncer.deleteThenIgnore(pin)
                TimelineService.ActionResponse(
                        success = true,
                        attributes = listOf(
                                TimelineAttributeFactory.subtitle("Removed from timeline"),
                                TimelineAttributeFactory.largeIcon(TimelineIcon.ResultDeleted),
                        )
                )
            }

            CalendarAction.Mute -> {
                TimelineService.ActionResponse(
                        success = true,
                        attributes = listOf(
                                TimelineAttributeFactory.subtitle("TODO"),
                                TimelineAttributeFactory.largeIcon(TimelineIcon.Settings),
                        )
                )
            }
        }
    }

    private fun updateAttendeeStatus(eventId: String, eventStatus: Int, attendee: String): Boolean {
        val contentResolver = (platformContext as AndroidPlatformContext).applicationContext.contentResolver
        val uri = CalendarContract.Attendees.CONTENT_URI
        val values = ContentValues().apply {
            put(CalendarContract.Attendees.ATTENDEE_STATUS, eventStatus)
        }
        val select = "${CalendarContract.Attendees.EVENT_ID} = ? AND ${CalendarContract.Attendees.ATTENDEE_EMAIL} = ?"
        val updated = contentResolver.update(uri, values, select, arrayOf(eventId, attendee))
        return if (updated == 0) {
            Logging.e("Failed to update attendee status")
            false
        } else {
            true
        }
    }
}