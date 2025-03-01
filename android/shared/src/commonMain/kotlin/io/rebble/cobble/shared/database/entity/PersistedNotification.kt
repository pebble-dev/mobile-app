package io.rebble.cobble.shared.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PersistedNotification(
    @PrimaryKey
    val sbnKey: String,
    val packageName: String,
    val postTime: Long,
    val title: String,
    val text: String,
    val groupKey: String?
)