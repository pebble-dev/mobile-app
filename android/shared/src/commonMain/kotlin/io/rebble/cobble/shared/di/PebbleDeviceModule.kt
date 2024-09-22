package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.handlers.*
import org.koin.core.qualifier.named
import org.koin.dsl.module

val pebbleDeviceModule = module {

    factory<Set<CobbleHandler>>(named("commonNegotiationDeviceHandlers")) { params ->
        setOf(
                SystemHandler(params.get()),
        )
    }
    factory<Set<CobbleHandler>>(named("commonDeviceHandlers")) { params ->
        get<Set<CobbleHandler>>(named("commonNegotiationDeviceHandlers")) +
        setOf(
                SystemHandler(params.get()),
                AppRunStateHandler(params.get()),
                AppInstallHandler(params.get()),
                CalendarActionHandler(params.get())
        )
    }
}