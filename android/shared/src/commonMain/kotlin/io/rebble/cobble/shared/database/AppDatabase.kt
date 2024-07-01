package io.rebble.cobble.shared.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.rebble.cobble.shared.database.dao.CalendarDao
import io.rebble.cobble.shared.database.dao.TimelinePinDao
import io.rebble.cobble.shared.database.entity.Calendar
import io.rebble.cobble.shared.database.entity.TimelinePin
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.mp.KoinPlatformTools

@Database(
        entities = [
            Calendar::class,
            TimelinePin::class
                   ],
        version = 1,
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun calendarDao(): CalendarDao
    abstract fun timelinePinDao(): TimelinePinDao
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