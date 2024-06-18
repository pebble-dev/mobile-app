package io.rebble.cobble.shared.database.dao

import androidx.room.*
import io.rebble.cobble.shared.database.entity.Calendar

@Dao
interface CalendarDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceCalendars(calendars: List<Calendar>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(calendar: Calendar)

    @Query("SELECT * FROM Calendar")
    suspend fun getAll(): List<Calendar>

    @Query("DELETE FROM Calendar")
    suspend fun deleteAll()

    @Update
    suspend fun update(calendar: Calendar)

    @Delete
    suspend fun delete(calendar: Calendar)
}