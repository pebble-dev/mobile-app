package io.rebble.cobble.shared.data

import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.entity.Calendar
import io.rebble.cobble.shared.database.entity.TimelinePin
import io.rebble.cobble.shared.domain.calendar.CalendarTimelineActionId
import io.rebble.cobble.shared.domain.common.SystemAppIDs.calendarWatchappId
import io.rebble.cobble.shared.domain.timeline.TimelineIcon
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import io.rebble.libpebblecommon.util.trimWithEllipsis
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.DurationUnit

data class CalendarEvent(
        val id: Long,
        val calendarId: Long,
        val title: String,
        val description: String,
        val location: String?,
        val startTime: Instant,
        val endTime: Instant,
        val allDay: Boolean,
        val attendees: List<EventAttendee>,
        val recurrenceRule: EventRecurrenceRule?,
        val reminders: List<EventReminder>,
        val availability: Availability,
        val status: Status,
) {
    enum class Availability {
        Free,
        Busy,
        Tentative,
        Unavailable,
    }

    enum class Status {
        None,
        Confirmed,
        Cancelled,
        Tentative,
    }
}

private fun transformDescription(rawDescription: String): String {
    val regex = Regex("<[^>]*>", setOf(RegexOption.MULTILINE))
    return rawDescription.replace(regex, "").trimWithEllipsis(300)
}

private fun CalendarEvent.makeAttributes(calendar: Calendar): List<TimelineAttribute> {
    val headings = mutableListOf<String>()
    val paragraphs = mutableListOf<String>()

    if (description.isNotBlank())  {
        headings.add("")
        paragraphs.add(transformDescription(description))
    }

    if (attendees.isNotEmpty()) {
        val attendeesString = attendees.mapNotNull { attendee ->
            if (!attendee.name.isNullOrBlank()) {
                attendee.name
            } else if (!attendee.email.isNullOrBlank()) {
                attendee.email
            } else {
                null
            }
        }.joinToString(", ")
        if (attendeesString.isNotBlank()) {
            headings.add("Attendees")
            paragraphs.add(attendeesString)
        }

        val selfAttendee = attendees.find { it.isCurrentUser }
        if (selfAttendee?.attendanceStatus != null) {
            headings.add("Status")
            paragraphs.add(selfAttendee.attendanceStatus.name)
        }
    }

    if (recurrenceRule != null) {
        headings.add("Recurrence")
        paragraphs.add(recurrenceRule.recurrenceFrequency.name)
    }

    headings.add("Calendar")
    paragraphs.add(calendar.name)

    return buildList {
        add(TimelineAttribute.tinyIcon(TimelineIcon.TimelineCalendar))
        add(TimelineAttribute.title(title))
        if (!location.isNullOrBlank()) {
            add(TimelineAttribute.locationName(location))
        }
        if (recurrenceRule != null) {
            add(TimelineAttribute.displayRecurring(true))
        }
        add(TimelineAttribute.headings(headings))
        add(TimelineAttribute.paragraphs(paragraphs))
    }
}

private fun CalendarEvent.makeActions(): List<TimelineAction> {
    val selfAttendee = attendees.find { it.isCurrentUser }
    return buildList {
        if (selfAttendee != null) {
            if (selfAttendee.attendanceStatus != EventAttendee.AttendanceStatus.Accepted) {
                add(TimelineAction(
                        CalendarTimelineActionId.AcceptEvent.id,
                        TimelineItem.Action.Type.Generic,
                        listOf(
                                TimelineAttribute.title("Accept")
                        )
                ))
            }

            if (selfAttendee.attendanceStatus != EventAttendee.AttendanceStatus.Tentative) {
                add(TimelineAction(
                        CalendarTimelineActionId.MaybeEvent.id,
                        TimelineItem.Action.Type.Generic,
                        listOf(
                                TimelineAttribute.title("Maybe")
                        )
                ))
            }

            if (selfAttendee.attendanceStatus != EventAttendee.AttendanceStatus.Declined) {
                add(TimelineAction(
                        CalendarTimelineActionId.DeclineEvent.id,
                        TimelineItem.Action.Type.Generic,
                        listOf(
                                TimelineAttribute.title("Decline")
                        )
                ))
            }
        }

        add(TimelineAction(
                CalendarTimelineActionId.Remove.id,
                TimelineItem.Action.Type.Generic,
                listOf(
                        TimelineAttribute.title("Remove")
                )
        ))

        add(TimelineAction(
                CalendarTimelineActionId.MuteCalendar.id,
                TimelineItem.Action.Type.Generic,
                listOf(
                        TimelineAttribute.title("Mute Calendar")
                )
        ))
    }
}

fun CalendarEvent.toTimelinePin(calendar: Calendar): TimelinePin {
    return TimelinePin(
            itemId = uuid4(),
            parentId = calendarWatchappId,
            backingId = generateCompositeBackingId(),
            timestamp = startTime,
            duration = if (allDay) null else (endTime - startTime).toInt(DurationUnit.MINUTES),
            type = TimelineItem.Type.Pin,
            isVisible = true,
            isFloating = false,
            isAllDay = allDay,
            persistQuickView = false,
            layout = TimelineItem.Layout.CalendarPin,
            attributesJson = Json.encodeToString(makeAttributes(calendar)),
            actionsJson = Json.encodeToString(makeActions()),
            nextSyncAction = NextSyncAction.Upload
    )
}


private fun CalendarEvent.generateCompositeBackingId() = "${calendarId}T${id}T${startTime}"