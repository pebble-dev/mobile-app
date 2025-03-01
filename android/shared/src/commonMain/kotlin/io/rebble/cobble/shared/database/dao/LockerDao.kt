package io.rebble.cobble.shared.database.dao

import androidx.room.*
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.entity.SyncedLockerEntry
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryPlatform
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface LockerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entry: SyncedLockerEntry)

    @Update
    suspend fun update(entry: SyncedLockerEntry)

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
    @Query("SELECT * FROM SyncedLockerEntry ORDER BY `order`")
    suspend fun getAllEntries(): List<SyncedLockerEntryWithPlatforms>

    @Transaction
    @Query("SELECT * FROM SyncedLockerEntry ORDER BY `order`")
    fun getAllEntriesFlow(): Flow<List<SyncedLockerEntryWithPlatforms>>

    @Query("DELETE FROM SyncedLockerEntryPlatform WHERE lockerEntryId = :entryId")
    suspend fun clearPlatformsFor(entryId: String)

    @Query("UPDATE SyncedLockerEntry SET nextSyncAction = :action WHERE id = :id")
    suspend fun setNextSyncAction(
        id: String,
        action: NextSyncAction
    )

    @Query("UPDATE SyncedLockerEntry SET nextSyncAction = :action WHERE id IN (:ids)")
    suspend fun setNextSyncAction(
        ids: Set<String>,
        action: NextSyncAction
    )

    @Transaction
    @Query("SELECT * FROM SyncedLockerEntry WHERE nextSyncAction in ('Upload', 'Delete')")
    suspend fun getEntriesForSync(): List<SyncedLockerEntryWithPlatforms>

    @Transaction
    @Query("SELECT * FROM SyncedLockerEntry WHERE uuid = :uuid")
    suspend fun getEntryByUuid(uuid: String): SyncedLockerEntryWithPlatforms?

    @Query("UPDATE SyncedLockerEntry SET `order` = :order WHERE id = :id")
    suspend fun updateOrder(
        id: String,
        order: Int
    )

    @Query("DELETE FROM SyncedLockerEntry")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM SyncedLockerEntry WHERE nextSyncAction = :action")
    suspend fun countEntriesWithNextSyncAction(action: NextSyncAction): Int

    @Query("SELECT * FROM SyncedLockerEntry WHERE nextSyncAction = 'Nothing'")
    suspend fun getSyncedEntries(): List<SyncedLockerEntry>

    @Query("UPDATE SyncedLockerEntry SET lastOpened = :time WHERE uuid = :uuid")
    suspend fun updateLastOpened(
        uuid: String,
        time: Instant?
    )
}