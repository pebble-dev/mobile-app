package io.rebble.cobble.shared.domain.calendar

import io.rebble.cobble.shared.database.entity.TimelinePin
import io.rebble.libpebblecommon.services.blobdb.TimelineService

interface PlatformCalendarActionExecutor {
    suspend fun handlePlatformAction(action: CalendarAction, pin: TimelinePin): TimelineService.ActionResponse
}