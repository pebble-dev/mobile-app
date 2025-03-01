package io.rebble.cobble.shared.database.entity

import androidx.room.Entity

@Entity(primaryKeys = ["packageId", "channelId"])
data class NotificationChannel(
    val packageId: String,
    val channelId: String,
    val name: String?,
    val description: String?,
    val conversationId: String?,
    val shouldNotify: Boolean
)