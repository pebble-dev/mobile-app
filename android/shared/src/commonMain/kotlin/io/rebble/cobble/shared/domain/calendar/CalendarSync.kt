package io.rebble.cobble.shared.domain.calendar

class CalendarSync(
        private val calendarSyncer: PhoneCalendarSyncer,
) {

    suspend fun onWatchConnected(unfaithful: Boolean) {
        // TODO
    }

    suspend fun deleteCalendarPinsFromWatch(): Boolean {
        TODO()
    }

    suspend fun doFullCalendarSync() {
        // TODO
    }

    suspend fun syncTimelineToWatch(): Boolean {
        TODO()
    }

}