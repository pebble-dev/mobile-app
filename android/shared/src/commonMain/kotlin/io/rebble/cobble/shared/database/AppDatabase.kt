package io.rebble.cobble.shared.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.rebble.cobble.shared.database.dao.*
import io.rebble.cobble.shared.database.entity.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.mp.KoinPlatformTools

@Database(
        entities = [
            Calendar::class,
            TimelinePin::class,
            PersistedNotification::class,
            CachedPackageInfo::class,
            NotificationChannel::class
                   ],
        version = 4,
        autoMigrations = [
            AutoMigration(1, 2),
            AutoMigration(2, 3),
            AutoMigration(3, 4)
        ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun calendarDao(): CalendarDao
    abstract fun timelinePinDao(): TimelinePinDao
    abstract fun persistedNotificationDao(): PersistedNotificationDao
    abstract fun cachedPackageInfoDao(): CachedPackageInfoDao
    abstract fun notificationChannelDao(): NotificationChannelDao

    companion object {
        fun instance(): AppDatabase {
            return KoinPlatformTools.defaultContext().get().get()
        }
    }
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

fun getDatabase(ioDispatcher: CoroutineDispatcher = Dispatchers.IO): AppDatabase {
    return getDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(ioDispatcher)
            .build()
}

fun closeDatabase() {
    KoinPlatformTools.defaultContext().get().get<AppDatabase>().close()
}