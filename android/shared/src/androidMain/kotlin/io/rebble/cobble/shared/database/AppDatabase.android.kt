package io.rebble.cobble.shared.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import org.koin.core.context.GlobalContext

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = GlobalContext.get().get<android.content.Context>()
    val dbFile = appContext.getDatabasePath("cobble-room.db")
    return Room.databaseBuilder<AppDatabase>(
            context = appContext,
            name = dbFile.absolutePath
    )

}