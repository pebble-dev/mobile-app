package io.rebble.cobble.shared.database.dao

import androidx.room.*
import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.entity.TimelinePin

@Dao
interface TimelinePinDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplacePins(pins: List<TimelinePin>)

    @Query("DELETE FROM TimelinePin")
    suspend fun deleteAll()

    @Query("SELECT * FROM TimelinePin WHERE nextSyncAction IN (:nextSyncActions)")
    suspend fun getAllPinsWithNextSyncAction(vararg nextSyncActions: NextSyncAction): List<TimelinePin>

    @Query("UPDATE TimelinePin SET nextSyncAction = :nextSyncAction WHERE itemId in (:itemIds)")
    suspend fun setSyncActionForPins(itemIds: List<Uuid>, nextSyncAction: NextSyncAction)

    @Update
    suspend fun updatePin(pin: TimelinePin)

    @Delete
    suspend fun deletePin(pin: TimelinePin)
    @Transaction
    @Delete
    suspend fun deletePins(pins: List<TimelinePin>)

    @Query("UPDATE TimelinePin SET nextSyncAction = :nextSyncAction WHERE parentId = :appId")
    suspend fun setSyncActionForAllPinsFromApp(appId: Uuid, nextSyncAction: NextSyncAction)

    @Query("DELETE FROM TimelinePin WHERE nextSyncAction = :nextSyncAction")
    suspend fun deletePinsWithNextSyncAction(nextSyncAction: NextSyncAction)

    @Query("UPDATE TimelinePin SET nextSyncAction = :replacement WHERE nextSyncAction = :current")
    suspend fun replaceNextSyncAction(current: NextSyncAction, replacement: NextSyncAction)

    @Query("SELECT * FROM TimelinePin WHERE parentId = :parentId")
    suspend fun getPinsForWatchapp(parentId: Uuid): List<TimelinePin>

    @Query("DELETE FROM TimelinePin WHERE parentId = :parentId")
    suspend fun deletePinsForWatchapp(parentId: Uuid)
}