package io.rebble.cobble.shared.domain.calendar

data class SelectableCalendar(
        val id: String,
        val name: String,
        val enabled: Boolean,
        val color: Int
)
