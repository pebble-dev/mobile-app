package io.rebble.cobble.shared.domain.calendar

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import com.philjay.Frequency
import com.philjay.RRule
import io.rebble.cobble.shared.AndroidPlatformContext
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.data.CalendarEvent
import io.rebble.cobble.shared.data.EventAttendee
import io.rebble.cobble.shared.data.EventRecurrenceRule
import io.rebble.cobble.shared.data.EventReminder
import io.rebble.cobble.shared.database.entity.Calendar
import kotlinx.datetime.*

private val calendarUri: Uri = CalendarContract.Calendars.CONTENT_URI
private val calendarProjection = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.OWNER_ACCOUNT,
        CalendarContract.Calendars.CALENDAR_COLOR
)

private val eventUri: Uri = CalendarContract.Instances.CONTENT_URI
private val eventProjection = arrayOf(
        CalendarContract.Instances._ID,
        CalendarContract.Instances.EVENT_ID,
        CalendarContract.Instances.CALENDAR_ID,
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.DESCRIPTION,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.END,
        CalendarContract.Instances.ALL_DAY,
        CalendarContract.Instances.EVENT_LOCATION,
        CalendarContract.Instances.AVAILABILITY,
        CalendarContract.Instances.STATUS,
        CalendarContract.Instances.RRULE,
        CalendarContract.Instances.RDATE
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
    calendar: Calendar,
    startDate: Instant,
    endDate: Instant
): List<CalendarEvent> {
platformContext as AndroidPlatformContext

    val contentResolver = platformContext.applicationContext.contentResolver
    val uriBuilder = eventUri.buildUpon()
    ContentUris.appendId(uriBuilder, startDate.toEpochMilliseconds())
    ContentUris.appendId(uriBuilder, endDate.toEpochMilliseconds())
    val builtUri = uriBuilder.build()

    val result = contentResolver.query(builtUri, eventProjection,
            "${CalendarContract.Instances.CALENDAR_ID} = ?"
            + " AND IFNULL(" + CalendarContract.Instances.STATUS + ", " + CalendarContract.Instances.STATUS_TENTATIVE + ") != " + CalendarContract.Instances.STATUS_CANCELED
            + " AND IFNULL(" + CalendarContract.Instances.SELF_ATTENDEE_STATUS + ", " + CalendarContract.Attendees.ATTENDEE_STATUS_NONE + ") != " + CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED,
            arrayOf(calendar.platformId), "BEGIN ASC"
    )?.use { cursor ->
        Logging.d("Found ${cursor.count} events for calendar ${calendar.name}")
        val list = mutableListOf<CalendarEvent>()
        while (cursor.moveToNext()) {
            val event = resolveCalendarEvent(contentResolver, cursor, calendar.ownerId)
            if (event != null) {
                list.add(event)
            }
        }
        list
    } ?: listOf()
    return result
}

private fun resolveCalendarEvent(contentResolver: ContentResolver, cursor: Cursor, ownerEmail: String): CalendarEvent? {
    val id = cursor.getNullableColumnIndex(CalendarContract.Instances._ID)
            ?.let { cursor.getLong(it) } ?: return null
    val eventId = cursor.getNullableColumnIndex(CalendarContract.Instances.EVENT_ID)
            ?.let { cursor.getLong(it) } ?: return null
    val calendarId = cursor.getNullableColumnIndex(CalendarContract.Instances.CALENDAR_ID)
            ?.let { cursor.getLong(it) } ?: return null
    val title = cursor.getNullableColumnIndex(CalendarContract.Instances.TITLE)
            ?.let { cursor.getString(it) } ?: "Untitled event"
    val description = cursor.getNullableColumnIndex(CalendarContract.Instances.DESCRIPTION)
            ?.let { cursor.getString(it) } ?: ""
    val start = cursor.getNullableColumnIndex(CalendarContract.Instances.BEGIN)
            ?.let { cursor.getLong(it) } ?: return null
    val end = cursor.getNullableColumnIndex(CalendarContract.Instances.END)
            ?.let { cursor.getLong(it) } ?: return null
    val allDay = cursor.getNullableColumnIndex(CalendarContract.Instances.ALL_DAY)
            ?.let { cursor.getInt(it) } ?: false
    val location = cursor.getNullableColumnIndex(CalendarContract.Instances.EVENT_LOCATION)
            ?.let { cursor.getString(it) }
    val availability = cursor.getNullableColumnIndex(CalendarContract.Instances.AVAILABILITY)
            ?.let { cursor.getInt(it) } ?: return null
    val status = cursor.getNullableColumnIndex(CalendarContract.Instances.STATUS)
            ?.let { cursor.getInt(it) } ?: return null
    val recurrenceRule = cursor.getNullableColumnIndex(CalendarContract.Instances.RRULE)
            ?.let { cursor.getString(it) }

    return CalendarEvent(
            id = id,
            calendarId = calendarId,
            title = title,
            description = description,
            location = location,
            startTime = Instant.fromEpochMilliseconds(start),
            endTime = Instant.fromEpochMilliseconds(end),
            allDay = allDay != 0,
            attendees = resolveAttendees(eventId, ownerEmail, contentResolver),
            recurrenceRule = recurrenceRule?.let { resolveRecurrenceRule(it, Instant.fromEpochMilliseconds(start)) },
            reminders = resolveReminders(eventId, contentResolver),
            availability = when (availability) {
                CalendarContract.Instances.AVAILABILITY_BUSY -> CalendarEvent.Availability.Busy
                CalendarContract.Instances.AVAILABILITY_FREE -> CalendarEvent.Availability.Free
                CalendarContract.Instances.AVAILABILITY_TENTATIVE -> CalendarEvent.Availability.Tentative
                else -> CalendarEvent.Availability.Unavailable
            },
            status = when (status) {
                CalendarContract.Instances.STATUS_CONFIRMED -> CalendarEvent.Status.Confirmed
                CalendarContract.Instances.STATUS_CANCELED -> CalendarEvent.Status.Cancelled
                CalendarContract.Instances.STATUS_TENTATIVE -> CalendarEvent.Status.Tentative
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
private fun resolveRecurrenceRule(rule: String, eventStart: Instant): EventRecurrenceRule {
    val rrule = RRule(rule)
    return EventRecurrenceRule(
            interval = rrule.interval,
            endDate = rrule.until?.let { Instant.fromEpochMilliseconds(it.toEpochMilli()) },
            totalOccurrences = rrule.count,
            recurrenceFrequency = when (rrule.freq) {
                Frequency.Daily -> EventRecurrenceRule.Frequency.Daily
                Frequency.Weekly -> EventRecurrenceRule.Frequency.Weekly(
                        rrule.byDay.mapNotNull { wdNum -> DayOfWeek.values().find { it.ordinal == wdNum.weekday.ordinal } }.toSet()
                )
                Frequency.Monthly -> EventRecurrenceRule.Frequency.Monthly(
                        rrule.byMonthDay.firstOrNull(),
                        rrule.byDay.mapNotNull { wdNum -> DayOfWeek.values().find { it.ordinal == wdNum.weekday.ordinal } }.toSet(),
                        rrule.bySetPos.firstOrNull()
                )
                Frequency.Yearly -> EventRecurrenceRule.Frequency.Yearly(
                        rrule.byMonth.firstOrNull()?.let { bm -> Month.values().find { it.ordinal == bm } },
                        rrule.byMonthDay.firstOrNull() ?: eventStart.toLocalDateTime(TimeZone.UTC).dayOfMonth,
                        rrule.byDay.mapNotNull { wdNum -> DayOfWeek.values().find { it.ordinal == wdNum.weekday.ordinal } }.toSet(),
                        rrule.bySetPos.firstOrNull()
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