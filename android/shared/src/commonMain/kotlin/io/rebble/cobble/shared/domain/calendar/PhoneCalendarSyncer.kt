package io.rebble.cobble.shared.domain.calendar

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.data.calendarWatchappId
import io.rebble.cobble.shared.data.toTimelinePin
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.dao.CalendarDao
import io.rebble.cobble.shared.database.dao.TimelinePinDao
import io.rebble.cobble.shared.database.entity.Calendar
import io.rebble.cobble.shared.database.entity.isInPast
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

private fun Instant.moveToStartOfDay(): Instant {
    val localDateTime = toLocalDateTime(TimeZone.currentSystemDefault())
    val dayMs = localDateTime.time.toMillisecondOfDay()
    return this - dayMs.milliseconds
}

class PhoneCalendarSyncer(
        private val platformContext: PlatformContext,
): KoinComponent {
    private val calendarDao: CalendarDao by inject()
    private val timelinePinDao: TimelinePinDao by inject()
    suspend fun syncDeviceCalendarsToDb(): Boolean {
        val existingCalendars = calendarDao.getAll()
        val calendars = getCalendars(platformContext)
        Logging.d("Got ${calendars.size} calendars from device, syncing... (${existingCalendars.size} existing)")
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
                calendarDao.update(updateCal)
            } else {
                calendarDao.delete(existingCalendar)
            }
        }
        calendars.forEach { newCalendar ->
            if (existingCalendars.none { it.platformId == newCalendar.platformId }) {
                calendarDao.insertOrReplace(newCalendar.copy(enabled = shouldCalendarBeEnabled(newCalendar)))
            }
        }

        val allCalendars = calendarDao.getAll()
        var anyChanges = false
        val existingPins = timelinePinDao.getPinsForWatchapp(calendarWatchappId)
        val startDate = Clock.System.now()
        val endDate = (startDate + 7.days).moveToStartOfDay()
        val newPins = allCalendars.flatMap { calendar ->
            if (!calendar.enabled) {
                return@flatMap emptyList()
            }

            val events = getCalendarEvents(platformContext, calendar, startDate, endDate)


            events.map { event ->
                event.toTimelinePin(calendar)
            }
        }
        val toInsert = newPins.mapNotNull { newPin ->
            val existingPin = existingPins.find { it.backingId == newPin.backingId }
            if (existingPin != null &&
                    existingPin.duration == newPin.duration &&
                    existingPin.isAllDay == newPin.isAllDay &&
                    existingPin.attributesJson == newPin.attributesJson &&
                    existingPin.actionsJson == newPin.actionsJson) {
                return@mapNotNull null
            }
            val pin = existingPin?.let {
                var pin = newPin.copy(
                        itemId = it.itemId,
                )
                if (it.nextSyncAction == NextSyncAction.Ignore ||
                        it.nextSyncAction == NextSyncAction.DeleteThenIgnore) {
                    pin = pin.copy(nextSyncAction = it.nextSyncAction)
                }
                pin
            } ?: newPin
            Logging.d("New Pin: {itemId: ${pin.itemId}, layout: ${pin.layout}, duration: ${pin.duration}, nextSyncAction: ${pin.nextSyncAction}} (existed: ${existingPin != null})")
            return@mapNotNull pin
        }
        if (toInsert.isNotEmpty()) {
            anyChanges = true
            timelinePinDao.insertOrReplacePins(toInsert)
        }

        val pinsToDelete = existingPins.filter { pin ->
            if (newPins.none { it.backingId == pin.backingId }) {
                Logging.d("Deleting pin ${pin.itemId} (backingId: ${pin.backingId}) as no longer exists in calendar")
                true
            } else if (pin.isInPast) {
                Logging.d("Deleting pin ${pin.itemId} (backingId: ${pin.backingId}) as it's in the past")
                true
            } else {
                false
            }
        }
        if (pinsToDelete.isNotEmpty()) {
            anyChanges = true
            timelinePinDao.deletePins(pinsToDelete)
        }
        Logging.d("Synced ${allCalendars.size} calendars to DB, changes: $anyChanges, total pins: ${timelinePinDao.getPinsForWatchapp(calendarWatchappId).size}")
        return anyChanges
    }

    private suspend fun shouldCalendarBeEnabled(calendar: Calendar): Boolean {
        //TODO
        return true
    }
}