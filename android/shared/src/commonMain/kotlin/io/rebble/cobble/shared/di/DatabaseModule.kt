package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.database.AppDatabase
import io.rebble.cobble.shared.database.getDatabase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val databaseModule = module {
    single { getDatabase() }
    single { get<AppDatabase>().calendarDao() }
    single { get<AppDatabase>().timelinePinDao() }
    single { get<AppDatabase>().lockerDao() }
    single { get<AppDatabase>().notificationChannelDao() }
    single { get<AppDatabase>().persistedNotificationDao() }
}