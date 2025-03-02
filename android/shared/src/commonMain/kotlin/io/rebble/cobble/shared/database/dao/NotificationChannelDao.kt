package io.rebble.cobble.shared.database.dao

import androidx.room.*
import io.rebble.cobble.shared.database.entity.NotificationChannel

@Dao
interface NotificationChannelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationChannel)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIfNotExists(notifications: List<NotificationChannel>)

    @Update
    suspend fun update(notification: NotificationChannel)

    @Delete
    suspend fun delete(notification: NotificationChannel)

    @Query(
        "SELECT * FROM NotificationChannel WHERE packageId = :packageId AND channelId = :channelId"
    )
    suspend fun get(
        packageId: String,
        channelId: String
    ): NotificationChannel?

    @Query(
        "UPDATE NotificationChannel SET shouldNotify = :shouldNotify WHERE packageId = :packageId AND channelId = :channelId"
    )
    suspend fun setShouldNotify(
        packageId: String,
        channelId: String,
        shouldNotify: Boolean
    )

    @Query(
        "SELECT conversationId FROM NotificationChannel WHERE packageId = :packageId AND channelId = :channelId"
    )
    suspend fun getConversationId(
        packageId: String,
        channelId: String
    ): String?
}