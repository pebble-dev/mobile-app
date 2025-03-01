package io.rebble.cobble.shared.data

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.Month

data class EventRecurrenceRule(
    val totalOccurrences: Int?,
    val interval: Int?,
    val endDate: Instant?,
    val recurrenceFrequency: Frequency
) {
    open class Frequency {
        val name: String
            get() =
                when (this) {
                    is Daily -> "Daily"
                    is Weekly -> "Weekly"
                    is Monthly -> "Monthly"
                    is Yearly -> "Yearly"
                    else -> error("Unknown frequency type")
                }

        /**
         * Repeats daily
         */
        object Daily : Frequency()

        /**
         * Repeats weekly
         * @param days Set of days of the week when the event should repeat
         */
        class Weekly(val days: Set<DayOfWeek>) : Frequency()

        /**
         * Repeats monthly
         * @param dayOfMonth Day of the month when the event should repeat
         * @param days Set of days of the week when the event should repeat
         * @param weekOfMonth Week of the month when the event should repeat, if dayOfMonth is null
         */
        class Monthly(
            val dayOfMonth: Int?,
            val days: Set<DayOfWeek>?,
            weekOfMonth: Int?
        ) : Frequency()

        /**
         * Repeats yearly
         * @param month Month when the event should repeat
         * @param dayOfMonth Day of the month when the event should repeat
         * @param days Set of days of the week when the event should repeat
         * @param weekOfMonth Week of the month when the event should repeat, if dayOfMonth is null
         */
        class Yearly(
            val month: Month?,
            val dayOfMonth: Int?,
            val days: Set<DayOfWeek>?,
            weekOfMonth: Int?
        ) : Frequency()
    }
}