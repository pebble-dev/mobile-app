package io.rebble.cobble.shared.database

import androidx.room.*
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.rebble.cobble.shared.database.dao.*
import io.rebble.cobble.shared.database.entity.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.mp.KoinPlatformTools

@Database(
        entities = [
            Calendar::class,
            TimelinePin::class,
            PersistedNotification::class,
            CachedPackageInfo::class,
            NotificationChannel::class,
            SyncedLockerEntry::class,
            SyncedLockerEntryPlatform::class
                   ],
        version = 10,
        autoMigrations = [
            AutoMigration(1, 2),
            AutoMigration(2, 3),
            AutoMigration(3, 4),
            AutoMigration(4, 5),
            AutoMigration(5, 6),
            AutoMigration(6, 7),
            AutoMigration(7, 8),
            AutoMigration(8, 9),
            AutoMigration(9, 10)
        ]
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseCtor::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun calendarDao(): CalendarDao
    abstract fun timelinePinDao(): TimelinePinDao
    abstract fun persistedNotificationDao(): PersistedNotificationDao
    abstract fun cachedPackageInfoDao(): CachedPackageInfoDao
    abstract fun notificationChannelDao(): NotificationChannelDao
    abstract fun lockerDao(): LockerDao

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

expect object AppDatabaseCtor: RoomDatabaseConstructor<AppDatabase>