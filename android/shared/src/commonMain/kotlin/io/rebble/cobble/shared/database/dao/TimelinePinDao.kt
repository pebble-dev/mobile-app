package io.rebble.cobble.shared.database.dao

import androidx.room.*
import io.rebble.cobble.shared.database.entity.TimelinePin

@Dao
interface TimelinePinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplacePins(pins: List<TimelinePin>)

    @Query("DELETE FROM TimelinePin")
    suspend fun deleteAll()
}