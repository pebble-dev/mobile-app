package io.rebble.cobble.shared.domain.calendar

enum class CalendarAction {
    Remove,
    Mute,
    Accept,
    Maybe,
    Decline;

    companion object {
        fun fromID(id: Int): CalendarAction = entries.first {it.ordinal == id}
    }
}