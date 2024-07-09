package io.rebble.cobble.shared.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.rebble.cobble.shared.database.entity.PersistedNotification

@Dao
interface PersistedNotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: PersistedNotification)

    @Query("SELECT * FROM PersistedNotification WHERE sbnKey = :key")
    suspend fun get(key: String): PersistedNotification?

    @Query("SELECT * FROM PersistedNotification WHERE sbnKey = :key AND packageName = :packageName AND title = :title AND text = :text")
    suspend fun getDuplicates(key:String, packageName: String, title: String, text: String): List<PersistedNotification>

    @Query("DELETE FROM PersistedNotification WHERE postTime < :time")
    suspend fun deleteOlderThan(time: Long)
}