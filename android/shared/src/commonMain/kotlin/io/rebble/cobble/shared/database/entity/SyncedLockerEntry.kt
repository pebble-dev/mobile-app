package io.rebble.cobble.shared.database.entity

import androidx.room.*
import io.rebble.cobble.shared.database.NextSyncAction

@Entity(
        indices = [
            Index(value = ["uuid"], unique = true),
        ]
)
data class SyncedLockerEntry(
        @PrimaryKey
        val id: String,
        val uuid: String,
        val version: String,
        val title: String,
        val type: String,
        val hearts: Int,
        val developerName: String,
        val configurable: Boolean,
        val timelineEnabled: Boolean,
        val removeLink: String,
        val shareLink: String,
        val pbwLink: String,
        val pbwReleaseId: String,
        val nextSyncAction: NextSyncAction
)

data class SyncedLockerEntryWithPlatforms(
        @Embedded
        val entry: SyncedLockerEntry,
        @Relation(
                parentColumn = "id",
                entityColumn = "lockerEntryId"
        )
        val platforms: List<SyncedLockerEntryPlatform>
)

@Entity(
        foreignKeys = [
            androidx.room.ForeignKey(
                    entity = SyncedLockerEntry::class,
                    parentColumns = ["id"],
                    childColumns = ["lockerEntryId"],
                    onDelete = androidx.room.ForeignKey.CASCADE
            )
        ],
        indices = [
            Index(value = ["lockerEntryId"]),
        ]
)
data class SyncedLockerEntryPlatform(
        @PrimaryKey(autoGenerate = true)
        val platformEntryId: Int,
        val lockerEntryId: String,
        val sdkVersion: String,
        val processInfoFlags: Int,
        val name: String,
        val description: String,
        val icon: String,
)

/**
 * Compare the data of this [SyncedLockerEntry] with another [SyncedLockerEntry] ignoring auto-generated fields.
 */
fun SyncedLockerEntryWithPlatforms.dataEqualTo(other: SyncedLockerEntryWithPlatforms): Boolean {
    return entry == other.entry &&
            platforms.all { platform ->
                other.platforms.any { it.dataEqualTo(platform) }
            }
}

/**
 * Compare the data of this [SyncedLockerEntryPlatform] with another [SyncedLockerEntryPlatform] ignoring auto-generated fields.
 */
fun SyncedLockerEntryPlatform.dataEqualTo(other: SyncedLockerEntryPlatform): Boolean {
    return lockerEntryId == other.lockerEntryId &&
            sdkVersion == other.sdkVersion &&
            processInfoFlags == other.processInfoFlags &&
            name == other.name &&
            description == other.description &&
            icon == other.icon
}