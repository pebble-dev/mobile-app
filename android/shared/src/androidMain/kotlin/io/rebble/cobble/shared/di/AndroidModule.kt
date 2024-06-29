package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.AndroidPlatformContext
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

val androidModule = module {
    factory {
        AndroidPlatformContext(androidContext().applicationContext)
    }
}