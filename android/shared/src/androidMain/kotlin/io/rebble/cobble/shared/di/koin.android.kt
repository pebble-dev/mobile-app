package io.rebble.cobble.shared.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

fun initKoin(context: Context) {
    startKoin {
        androidContext(context)
        modules(
            calendarModule,
            stateModule,
            databaseModule,
            dataStoreModule,
            androidModule,
            libpebbleModule,
            dependenciesModule,
            viewModelModule,
        )
    }
}