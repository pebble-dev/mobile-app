package io.rebble.cobble.shared.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity
data class CachedPackageInfo(
    @PrimaryKey val id: String,
    val name: String,
    val flags: Int,
    val updated: Instant
)