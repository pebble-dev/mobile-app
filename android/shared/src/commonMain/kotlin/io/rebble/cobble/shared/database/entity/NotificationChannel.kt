package io.rebble.cobble.shared.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(primaryKeys = ["packageId", "channelId"])
data class NotificationChannel(
        val packageId: String,
        val channelId: String,
        val name: String?,
        val description: String?,
        val conversationId: String?,
        val shouldNotify: Boolean,
)
