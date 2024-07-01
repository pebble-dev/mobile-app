package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.AndroidPlatformContext
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.datastore.createDataStore
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind

val androidModule = module {
    factory {
        AndroidPlatformContext(androidContext().applicationContext)
    } bind PlatformContext::class
    single { createDataStore(androidContext()) }
}