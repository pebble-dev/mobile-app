package io.rebble.cobble.shared.domain.calendar

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import com.philjay.Frequency
import com.philjay.RRule
import io.rebble.cobble.shared.AndroidPlatformContext
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.data.CalendarEvent
import io.rebble.cobble.shared.data.EventAttendee
import io.rebble.cobble.shared.data.EventRecurrenceRule
import io.rebble.cobble.shared.data.EventReminder
import io.rebble.cobble.shared.database.entity.Calendar
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.Month

private val calendarUri: Uri = CalendarContract.Calendars.CONTENT_URI
private val calendarProjection = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.OWNER_ACCOUNT,
        CalendarContract.Calendars.CALENDAR_COLOR
)

private val eventUri: Uri = CalendarContract.Events.CONTENT_URI
private val eventProjection = arrayOf(
        CalendarContract.Events._ID,
        CalendarContract.Events.CALENDAR_ID,
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DESCRIPTION,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.DTEND,
        CalendarContract.Events.ALL_DAY,
        CalendarContract.Events.EVENT_LOCATION,
        CalendarContract.Events.AVAILABILITY,
        CalendarContract.Events.STATUS,
        CalendarContract.Events.RRULE,
)

private val attendeeUri: Uri = CalendarContract.Attendees.CONTENT_URI
private val attendeeProjection = arrayOf(
        CalendarContract.Attendees._ID,
        CalendarContract.Attendees.EVENT_ID,
        CalendarContract.Attendees.ATTENDEE_NAME,
        CalendarContract.Attendees.ATTENDEE_EMAIL,
        CalendarContract.Attendees.ATTENDEE_TYPE,
        CalendarContract.Attendees.ATTENDEE_STATUS,
        CalendarContract.Attendees.ATTENDEE_RELATIONSHIP,
)

private val reminderUri: Uri = CalendarContract.Reminders.CONTENT_URI
private val reminderProjection = arrayOf(
        CalendarContract.Reminders._ID,
        CalendarContract.Reminders.EVENT_ID,
        CalendarContract.Reminders.MINUTES,
        CalendarContract.Reminders.METHOD,
)

actual suspend fun getCalendars(platformContext: PlatformContext): List<Calendar> {
    platformContext as AndroidPlatformContext

    val contentResolver = platformContext.applicationContext.contentResolver
    return contentResolver.query(calendarUri, calendarProjection, null, null, null)?.use { cursor ->
        return@use generateSequence {
            if (cursor.moveToNext()) {
                val id = cursor.getNullableColumnIndex(CalendarContract.Calendars._ID)
                        ?.let { cursor.getLong(it) } ?: return@generateSequence null
                val accountName = cursor.getNullableColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
                        ?.let { cursor.getString(it) } ?: return@generateSequence null
                val displayName = cursor.getNullableColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                        ?.let { cursor.getString(it) } ?: return@generateSequence null
                val ownerAccount = cursor.getNullableColumnIndex(CalendarContract.Calendars.OWNER_ACCOUNT)
                        ?.let { cursor.getString(it) } ?: return@generateSequence null
                val color = cursor.getNullableColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR)
                        ?.let { cursor.getInt(it) } ?: return@generateSequence null

                Calendar(
                        id = 0,
                        platformId = id.toString(),
                        name = displayName,
                        ownerName = accountName,
                        ownerId = ownerAccount,
                        color = color,
                        enabled = true
                )
            } else {
                null
            }
        }.toList()
    } ?: listOf()
}

private fun Cursor.getNullableColumnIndex(columnName: String): Int? {
    val index = getColumnIndex(columnName)
    return if (index == -1) {
        null
    } else {
        index
    }
}

actual suspend fun getCalendarEvents(
    platformContext: PlatformContext,
    calendar: Calendar
): List<CalendarEvent> {
platformContext as AndroidPlatformContext

    val contentResolver = platformContext.applicationContext.contentResolver
    return contentResolver.query(eventUri, eventProjection, "${CalendarContract.Events.CALENDAR_ID} = ?", arrayOf(calendar.platformId), null)?.use { cursor ->
        return@use generateSequence {
            if (cursor.moveToNext()) {
                resolveCalendarEvent(contentResolver, cursor, calendar.ownerId)
            } else {
                null
            }
        }.toList()
    } ?: listOf()
}

private fun resolveCalendarEvent(contentResolver: ContentResolver, cursor: Cursor, ownerEmail: String): CalendarEvent? {
    val id = cursor.getNullableColumnIndex(CalendarContract.Events._ID)
            ?.let { cursor.getLong(it) } ?: return null
    val calendarId = cursor.getNullableColumnIndex(CalendarContract.Events.CALENDAR_ID)
            ?.let { cursor.getLong(it) } ?: return null
    val title = cursor.getNullableColumnIndex(CalendarContract.Events.TITLE)
            ?.let { cursor.getString(it) } ?: return null
    val description = cursor.getNullableColumnIndex(CalendarContract.Events.DESCRIPTION)
            ?.let { cursor.getString(it) } ?: return null
    val dtStart = cursor.getNullableColumnIndex(CalendarContract.Events.DTSTART)
            ?.let { cursor.getLong(it) } ?: return null
    val dtEnd = cursor.getNullableColumnIndex(CalendarContract.Events.DTEND)
            ?.let { cursor.getLong(it) } ?: return null
    val allDay = cursor.getNullableColumnIndex(CalendarContract.Events.ALL_DAY)
            ?.let { cursor.getInt(it) } ?: return null
    val location = cursor.getNullableColumnIndex(CalendarContract.Events.EVENT_LOCATION)
            ?.let { cursor.getString(it) } ?: return null
    val availability = cursor.getNullableColumnIndex(CalendarContract.Events.AVAILABILITY)
            ?.let { cursor.getInt(it) } ?: return null
    val status = cursor.getNullableColumnIndex(CalendarContract.Events.STATUS)
            ?.let { cursor.getInt(it) } ?: return null
    val recurrenceRule = cursor.getNullableColumnIndex(CalendarContract.Events.RRULE)
            ?.let { cursor.getString(it) } ?: return null

    return CalendarEvent(
            id = id,
            calendarId = calendarId,
            title = title,
            description = description,
            location = location,
            startTime = Instant.fromEpochMilliseconds(dtStart),
            endTime = Instant.fromEpochMilliseconds(dtEnd),
            allDay = allDay != 0,
            attendees = resolveAttendees(id, ownerEmail, contentResolver),
            recurrenceRule = resolveRecurrenceRule(recurrenceRule),
            reminders = resolveReminders(id, contentResolver),
            availability = when (availability) {
                CalendarContract.Events.AVAILABILITY_BUSY -> CalendarEvent.Availability.Busy
                CalendarContract.Events.AVAILABILITY_FREE -> CalendarEvent.Availability.Free
                CalendarContract.Events.AVAILABILITY_TENTATIVE -> CalendarEvent.Availability.Tentative
                else -> CalendarEvent.Availability.Unavailable
            },
            status = when (status) {
                CalendarContract.Events.STATUS_CONFIRMED -> CalendarEvent.Status.Confirmed
                CalendarContract.Events.STATUS_CANCELED -> CalendarEvent.Status.Cancelled
                CalendarContract.Events.STATUS_TENTATIVE -> CalendarEvent.Status.Tentative
                else -> CalendarEvent.Status.None
            }
    )
}

private fun resolveAttendees(eventId: Long, ownerEmail: String, contentResolver: ContentResolver): List<EventAttendee> {
    return contentResolver.query(attendeeUri, attendeeProjection, "${CalendarContract.Attendees.EVENT_ID} = ?", arrayOf(eventId.toString()), null)?.use { cursor ->
        return@use generateSequence {
            if (cursor.moveToNext()) {
                val id = cursor.getNullableColumnIndex(CalendarContract.Attendees._ID)
                        ?.let { cursor.getLong(it) } ?: return@generateSequence null
                val eventId = cursor.getNullableColumnIndex(CalendarContract.Attendees.EVENT_ID)
                        ?.let { cursor.getLong(it) } ?: return@generateSequence null
                val name = cursor.getNullableColumnIndex(CalendarContract.Attendees.ATTENDEE_NAME)
                        ?.let { cursor.getString(it) } ?: return@generateSequence null
                val email = cursor.getNullableColumnIndex(CalendarContract.Attendees.ATTENDEE_EMAIL)
                        ?.let { cursor.getString(it) } ?: return@generateSequence null
                val type = cursor.getNullableColumnIndex(CalendarContract.Attendees.ATTENDEE_TYPE)
                        ?.let { cursor.getInt(it) } ?: return@generateSequence null
                val status = cursor.getNullableColumnIndex(CalendarContract.Attendees.ATTENDEE_STATUS)
                        ?.let { cursor.getInt(it) } ?: return@generateSequence null
                val relationship = cursor.getNullableColumnIndex(CalendarContract.Attendees.ATTENDEE_RELATIONSHIP)
                        ?.let { cursor.getInt(it) } ?: return@generateSequence null

                EventAttendee(
                        name = name,
                        email = email,
                        role = when (type) {
                            CalendarContract.Attendees.TYPE_REQUIRED -> EventAttendee.Role.Required
                            CalendarContract.Attendees.TYPE_OPTIONAL -> EventAttendee.Role.Optional
                            CalendarContract.Attendees.TYPE_RESOURCE -> EventAttendee.Role.Resource
                            else -> EventAttendee.Role.None
                        },
                        isOrganizer = relationship == CalendarContract.Attendees.RELATIONSHIP_ORGANIZER,
                        isCurrentUser = email == ownerEmail,
                        attendanceStatus = when (status) {
                            CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED -> EventAttendee.AttendanceStatus.Accepted
                            CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED -> EventAttendee.AttendanceStatus.Declined
                            CalendarContract.Attendees.ATTENDEE_STATUS_INVITED -> EventAttendee.AttendanceStatus.Invited
                            CalendarContract.Attendees.ATTENDEE_STATUS_TENTATIVE -> EventAttendee.AttendanceStatus.Tentative
                            else -> EventAttendee.AttendanceStatus.None
                        }
                )
            } else {
                null
            }
        }.toList()
    } ?: listOf()
}

/**
 * Resolve recurrence rule from string, see [RFC 5545](https://datatracker.ietf.org/doc/html/rfc5545#section-3.8.5.3)
 */
private fun resolveRecurrenceRule(rule: String): EventRecurrenceRule {
    val rrule = RRule(rule)
    return EventRecurrenceRule(
            interval = rrule.interval,
            endDate = rrule.until?.let { Instant.fromEpochMilliseconds(it.toEpochMilli()) },
            totalOccurrences = rrule.count,
            recurrenceFrequency = when (rrule.freq) {
                Frequency.Daily -> EventRecurrenceRule.Frequency.Daily
                Frequency.Weekly -> EventRecurrenceRule.Frequency.Weekly(
                        rrule.byDay.map { wdNum -> DayOfWeek.values()[wdNum.number - 1] }.toSet()
                )
                Frequency.Monthly -> EventRecurrenceRule.Frequency.Monthly(
                        rrule.byMonthDay.firstOrNull(),
                        rrule.byDay.map { wdNum -> DayOfWeek.values()[wdNum.number - 1] }.toSet(),
                        rrule.byWeekNo.firstOrNull()
                )
                Frequency.Yearly -> EventRecurrenceRule.Frequency.Yearly(
                        rrule.byMonth.firstOrNull()?.let { Month.values()[it - 1] } ?: error("Missing month"),
                        rrule.byMonthDay.firstOrNull(),
                        rrule.byDay.map { wdNum -> DayOfWeek.values()[wdNum.number - 1] }.toSet(),
                        rrule.byWeekNo.firstOrNull()
                )
                else -> error("Unsupported frequency: ${rrule.freq}")
            },
    )
}

private fun resolveReminders(eventId: Long, contentResolver: ContentResolver): List<EventReminder> {
    return contentResolver.query(
            reminderUri,
            reminderProjection,
            "${CalendarContract.Reminders.EVENT_ID} = ? AND ${CalendarContract.Reminders.METHOD} IN (${CalendarContract.Reminders.METHOD_ALERT}, ${CalendarContract.Reminders.METHOD_DEFAULT})",
            arrayOf(eventId.toString()),
            null
    )?.use { cursor ->
        return@use generateSequence {
            if (cursor.moveToNext()) {
                val id = cursor.getNullableColumnIndex(CalendarContract.Reminders._ID)
                        ?.let { cursor.getLong(it) } ?: return@generateSequence null
                val eventId = cursor.getNullableColumnIndex(CalendarContract.Reminders.EVENT_ID)
                        ?.let { cursor.getLong(it) } ?: return@generateSequence null
                val minutes = cursor.getNullableColumnIndex(CalendarContract.Reminders.MINUTES)
                        ?.let { cursor.getInt(it) } ?: return@generateSequence null
                val method = cursor.getNullableColumnIndex(CalendarContract.Reminders.METHOD)
                        ?.let { cursor.getInt(it) } ?: return@generateSequence null
                EventReminder(
                        minutesBefore = minutes,
                )
            } else {
                null
            }
        }.toList()
    } ?: listOf()
}