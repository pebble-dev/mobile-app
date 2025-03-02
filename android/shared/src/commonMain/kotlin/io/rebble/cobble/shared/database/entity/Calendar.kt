package io.rebble.cobble.shared.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["platformId"], unique = true)])
data class Calendar(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val platformId: String,
    val name: String,
    val ownerName: String,
    val ownerId: String,
    val color: Int,
    val enabled: Boolean
)