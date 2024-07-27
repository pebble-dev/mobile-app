package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.domain.calendar.PhoneCalendarSyncer
import io.rebble.cobble.shared.domain.timeline.WatchTimelineSyncer
import io.rebble.cobble.shared.domain.timeline.TimelineActionManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val calendarModule = module {
    singleOf(::PhoneCalendarSyncer)
    singleOf(::WatchTimelineSyncer)
    singleOf(::TimelineActionManager)
}