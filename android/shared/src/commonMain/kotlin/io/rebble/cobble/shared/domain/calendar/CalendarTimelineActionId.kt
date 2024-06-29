package io.rebble.cobble.shared.domain.calendar

enum class CalendarTimelineActionId(val id: Int) {
    Remove(0),
    MuteCalendar(1),
    AcceptEvent(2),
    MaybeEvent(3),
    DeclineEvent(4),
}