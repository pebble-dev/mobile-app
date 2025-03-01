package io.rebble.cobble.shared.database.dao

import androidx.room.*
import io.rebble.cobble.shared.database.entity.Calendar
import kotlinx.coroutines.flow.Flow

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

    @Query("UPDATE Calendar SET enabled = :enabled WHERE id = :calendarId")
    suspend fun setEnabled(
        calendarId: Long,
        enabled: Boolean
    )

    @Query("SELECT * FROM Calendar")
    fun getFlow(): Flow<List<Calendar>>

    @Query("SELECT * FROM Calendar WHERE id = :calendarId")
    suspend fun get(calendarId: Long): Calendar?
}