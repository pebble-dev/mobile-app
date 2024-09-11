package io.rebble.cobble.shared.database.dao

import androidx.room.*
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.entity.SyncedLockerEntry
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryPlatform
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms

@Dao
interface LockerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entry: SyncedLockerEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplacePlatform(platform: SyncedLockerEntryPlatform)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAll(entries: List<SyncedLockerEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAllPlatforms(platforms: List<SyncedLockerEntryPlatform>)

    @Transaction
    @Query("SELECT * FROM SyncedLockerEntry WHERE id = :id")
    suspend fun getEntry(id: String): SyncedLockerEntryWithPlatforms?

    @Transaction
    @Query("SELECT * FROM SyncedLockerEntry")
    suspend fun getAllEntries(): List<SyncedLockerEntryWithPlatforms>

    @Query("DELETE FROM SyncedLockerEntryPlatform WHERE lockerEntryId = :entryId")
    suspend fun clearPlatformsFor(entryId: String)

    @Query("UPDATE SyncedLockerEntry SET nextSyncAction = :action WHERE id = :id")
    suspend fun setNextSyncAction(id: String, action: NextSyncAction)

    @Query("UPDATE SyncedLockerEntry SET nextSyncAction = :action WHERE id IN (:ids)")
    suspend fun setNextSyncAction(ids: Set<String>, action: NextSyncAction)

    @Query("SELECT * FROM SyncedLockerEntry WHERE nextSyncAction in (1, 2)")
    suspend fun getEntriesForSync(): List<SyncedLockerEntryWithPlatforms>

    @Query("SELECT * FROM SyncedLockerEntry WHERE uuid = :uuid")
    suspend fun getEntryByUuid(uuid: String): SyncedLockerEntryWithPlatforms?
}