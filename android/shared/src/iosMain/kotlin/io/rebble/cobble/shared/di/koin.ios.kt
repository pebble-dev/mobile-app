package io.rebble.cobble.shared.di

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(
            calendarModule,
            stateModule,
            databaseModule,
            dataStoreModule,
            iosModule
        )
    }
}