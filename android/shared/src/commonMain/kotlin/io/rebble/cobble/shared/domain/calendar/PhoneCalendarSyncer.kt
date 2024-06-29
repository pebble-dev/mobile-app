package io.rebble.cobble.shared.domain.calendar

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.data.calendarWatchappId
import io.rebble.cobble.shared.data.toTimelinePin
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.entity.Calendar
import io.rebble.cobble.shared.database.getDatabase
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.minutes

class PhoneCalendarSyncer(
        private val platformContext: PlatformContext,
) {
    suspend fun syncDeviceCalendarsToDb(): Boolean {
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

        val allCalendars = dao.getAll()
        var anyChanges = false
        allCalendars.forEach { calendar ->
            if (!calendar.enabled) {
                return@forEach
            }

            val events = getCalendarEvents(platformContext, calendar)


            val newPins = events.map { event ->
                event.toTimelinePin(calendar)
            }
            val existingPins = getDatabase().timelinePinDao().getPinsForWatchapp(calendarWatchappId)

            for (newPin in newPins) {
                val existingPin = existingPins.find { it.backingId == newPin.backingId }
                if (existingPin != null &&
                        existingPin.duration == newPin.duration &&
                        existingPin.isAllDay == newPin.isAllDay &&
                        existingPin.attributesJson == newPin.attributesJson &&
                        existingPin.actionsJson == newPin.actionsJson) {
                    continue
                }

                val newItemId = existingPin?.itemId ?: uuid4()
                var pin = newPin.copy(
                        itemId = newItemId
                )
                if (existingPin != null &&
                        (existingPin.nextSyncAction == NextSyncAction.Ignore ||
                                existingPin.nextSyncAction == NextSyncAction.DeleteThenIgnore)) {
                    pin = pin.copy(nextSyncAction = existingPin.nextSyncAction)
                }
                getDatabase().timelinePinDao().insertOrReplacePins(listOf(pin))
                anyChanges = true
            }

            for (pin in existingPins) {
                if (newPins.none { it.backingId == pin.backingId }) { // Event was deleted
                    getDatabase().timelinePinDao().deletePin(pin)
                    anyChanges = true
                } else if (pin.timestamp + (pin.duration?.minutes ?: 0.minutes) < Clock.System.now()) { // Event is over
                    getDatabase().timelinePinDao().deletePin(pin)
                    anyChanges = true
                }
            }
        }
        return anyChanges
    }

    private suspend fun shouldCalendarBeEnabled(calendar: Calendar): Boolean {
        //TODO
        return true
    }
}