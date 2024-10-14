package io.rebble.cobble.shared.domain.calendar

import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.data.CalendarEvent
import io.rebble.cobble.shared.database.entity.Calendar
import kotlinx.datetime.Instant

actual suspend fun getCalendars(platformContext: PlatformContext): List<Calendar> {
    TODO("Not yet implemented")
}

actual suspend fun getCalendarEvents(
    platformContext: PlatformContext,
    calendar: Calendar,
    startDate: Instant,
    endDate: Instant
): List<CalendarEvent> {
    TODO("Not yet implemented")
}