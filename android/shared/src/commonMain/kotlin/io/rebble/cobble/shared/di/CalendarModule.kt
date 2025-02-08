package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.domain.calendar.CalendarSync
import io.rebble.cobble.shared.domain.calendar.PhoneCalendarSyncer
import io.rebble.cobble.shared.domain.notifications.NotificationActionHandler
import io.rebble.cobble.shared.domain.timeline.TimelineActionManager
import io.rebble.cobble.shared.domain.timeline.WatchTimelineSyncer
import io.rebble.cobble.shared.errors.GlobalExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val calendarModule = module {
    singleOf(::PhoneCalendarSyncer)
    factory { params ->
        TimelineActionManager(params.get())
    }
    single {
        CalendarSync(CoroutineScope(SupervisorJob() + get<GlobalExceptionHandler>() + CoroutineName("CalendarSync")))
    }
}