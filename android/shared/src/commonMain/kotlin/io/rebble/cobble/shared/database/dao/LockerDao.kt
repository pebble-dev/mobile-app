package io.rebble.cobble.shared.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.entity.SyncedLockerEntry
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms

@Dao
interface LockerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entry: SyncedLockerEntryWithPlatforms)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAll(entries: List<SyncedLockerEntryWithPlatforms>)

    @Query("SELECT * FROM SyncedLockerEntry WHERE id = :id")
    suspend fun getEntry(id: String): SyncedLockerEntryWithPlatforms?

    @Query("SELECT * FROM SyncedLockerEntry")
    suspend fun getAllEntries(): List<SyncedLockerEntryWithPlatforms>

    @Query("DELETE FROM SyncedLockerEntryPlatform WHERE lockerEntryId = :entryId")
    suspend fun clearPlatformsFor(entryId: String)

    @Query("UPDATE SyncedLockerEntry SET nextSyncAction = :action WHERE id = :id")
    suspend fun setNextSyncAction(id: String, action: NextSyncAction)

    @Query("UPDATE SyncedLockerEntry SET nextSyncAction = :action WHERE id IN (:ids)")
    suspend fun setNextSyncAction(ids: Set<String>, action: NextSyncAction)
}