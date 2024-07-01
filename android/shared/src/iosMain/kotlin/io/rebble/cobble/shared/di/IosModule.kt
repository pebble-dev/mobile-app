package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.IOSPlatformContext
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.datastore.createDataStore
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val iosModule = module {
    factoryOf<PlatformContext> {
        IOSPlatformContext()
    }
    single { createDataStore() }
}