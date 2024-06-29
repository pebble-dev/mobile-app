package io.rebble.cobble.shared.domain.calendar

import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.data.CalendarEvent
import io.rebble.cobble.shared.database.entity.Calendar

actual suspend fun getCalendars(platformContext: PlatformContext): List<Calendar> {
    TODO("Not yet implemented")
}

actual suspend fun getCalendarEvents(
    platformContext: PlatformContext,
    calendar: Calendar
): List<CalendarEvent> {
    TODO("Not yet implemented")
}