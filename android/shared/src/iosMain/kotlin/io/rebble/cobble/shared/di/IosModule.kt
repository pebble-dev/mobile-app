package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.IOSPlatformContext
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.datastore.createDataStore
import io.rebble.cobble.shared.handlers.*
import org.koin.core.qualifier.named
import org.koin.dsl.module

val iosModule =
    module {
        factory<PlatformContext> {
            IOSPlatformContext()
        }
        single { createDataStore() }

        factory<Set<CobbleHandler>>(named("deviceHandlers")) { params ->
            get<Set<CobbleHandler>>(named("negotiationDeviceHandlers")) +
                setOf(
                    AppRunStateHandler(params.get()),
                    AppInstallHandler(params.get()),
                    CalendarActionHandler(params.get())
                )
        }

        factory<Set<CobbleHandler>>(named("negotiationDeviceHandlers")) { params ->
            setOf(
                SystemHandler(params.get())
            )
        }
    }