package io.rebble.cobble.shared.database.entity

import androidx.room.*
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.util.AppCompatibility
import io.rebble.libpebblecommon.metadata.WatchType
import kotlinx.datetime.Instant

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
        val developerId: String?,
        val configurable: Boolean,
        val timelineEnabled: Boolean,
        val removeLink: String,
        val shareLink: String,
        val pbwLink: String,
        val pbwReleaseId: String,
        @ColumnInfo(defaultValue = "0")
        val pbwIconResourceId: Int,
        val nextSyncAction: NextSyncAction,
        @ColumnInfo(defaultValue = "-1")
        val order: Int,
        val lastOpened: Instant?,
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

fun SyncedLockerEntryWithPlatforms.getBestPlatformForDevice(watchType: WatchType): SyncedLockerEntryPlatform? {
    val platformName = AppCompatibility.getBestVariant(
            watchType,
            this.platforms.map { plt -> plt.name }
    )?.codename
    return this.platforms.firstOrNull { plt -> plt.name == platformName }
}

fun SyncedLockerEntryWithPlatforms.getVersion(): Pair<UByte, UByte> {
    val versionCode = Regex("""\d+\.\d+""").find(entry.version)?.value ?: "0.0"
    val appVersionMajor = versionCode.split(".")[0].toUByte()
    val appVersionMinor = versionCode.split(".")[1].toUByte()
    return Pair(appVersionMajor, appVersionMinor)
}

@Entity(
        foreignKeys = [
            ForeignKey(
                    entity = SyncedLockerEntry::class,
                    parentColumns = ["id"],
                    childColumns = ["lockerEntryId"],
                    onDelete = ForeignKey.CASCADE
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
        @Embedded
        val images: SyncedLockerEntryPlatformImages,
)

fun SyncedLockerEntryPlatform.getSdkVersion(): Pair<UByte, UByte> {
    val sdkVersionMajor = sdkVersion.split(".")[0].toUByte()
    val sdkVersionMinor = sdkVersion.split(".")[1].toUByte()
    return Pair(sdkVersionMajor, sdkVersionMinor)
}

data class SyncedLockerEntryPlatformImages(
        val icon: String?,
        val list: String?,
        val screenshot: String?,
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
            images.icon == other.images.icon &&
            images.list == other.images.list &&
            images.screenshot == other.images.screenshot
}