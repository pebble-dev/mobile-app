package io.rebble.cobble.shared.database

import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    TODO("Not yet implemented")
}

actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase {
        TODO("Not yet implemented")
    }

}