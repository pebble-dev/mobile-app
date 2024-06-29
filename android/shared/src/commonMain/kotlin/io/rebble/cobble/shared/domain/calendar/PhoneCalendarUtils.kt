package io.rebble.cobble.shared.domain.calendar

import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.data.CalendarEvent
import io.rebble.cobble.shared.database.entity.Calendar

expect suspend fun getCalendars(platformContext: PlatformContext): List<Calendar>
expect suspend fun getCalendarEvents(platformContext: PlatformContext, calendar: Calendar): List<CalendarEvent>