package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.IOSPlatformContext
import io.rebble.cobble.shared.PlatformContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val iosModule = module {
    factoryOf<PlatformContext> {
        IOSPlatformContext()
    }
}