package io.rebble.cobble.shared.data

data class EventReminder(
        /**
         * Minutes before the event when the reminder should trigger
         */
        val minutesBefore: Int,
)
