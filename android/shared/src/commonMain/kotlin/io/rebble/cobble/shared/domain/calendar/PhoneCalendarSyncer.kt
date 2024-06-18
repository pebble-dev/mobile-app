package io.rebble.cobble.shared.domain.calendar

import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.database.entity.Calendar
import io.rebble.cobble.shared.database.getDatabase

class PhoneCalendarSyncer(
        private val platformContext: PlatformContext,
) {
    suspend fun syncDeviceCalendarsToDb() {
        val dao = getDatabase().calendarDao()
        val existingCalendars = dao.getAll()
        val calendars = getCalendars(platformContext)
        existingCalendars.forEach { existingCalendar ->
            val matchingCalendar = calendars.find { it.platformId == existingCalendar.platformId }
            if (matchingCalendar != null) {
                val updateCal = existingCalendar.copy(
                        platformId = matchingCalendar.platformId,
                        name = matchingCalendar.name,
                        ownerName = matchingCalendar.ownerName,
                        ownerId = matchingCalendar.ownerId,
                        color = matchingCalendar.color,
                        enabled = shouldCalendarBeEnabled(matchingCalendar)
                )
                dao.update(updateCal)
            } else {
                dao.delete(existingCalendar)
            }
        }
        calendars.forEach { newCalendar ->
            if (existingCalendars.none { it.platformId == newCalendar.platformId }) {
                dao.insertOrReplace(newCalendar.copy(enabled = shouldCalendarBeEnabled(newCalendar)))
            }
        }
    }

    private suspend fun shouldCalendarBeEnabled(calendar: Calendar): Boolean {
        //TODO
        return true
    }
}