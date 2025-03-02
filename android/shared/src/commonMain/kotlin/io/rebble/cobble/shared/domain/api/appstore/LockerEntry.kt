package io.rebble.cobble.shared.domain.api.appstore

import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.entity.SyncedLockerEntry
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryPlatform
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryPlatformImages
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LockerEntry(
    val id: String,
    val uuid: String,
    @SerialName("user_token") val userToken: String,
    val title: String,
    val type: String,
    val category: String,
    val version: String? = null,
    val hearts: Int,
    @SerialName("is_configurable") val isConfigurable: Boolean,
    @SerialName("is_timeline_enabled") val isTimelineEnabled: Boolean,
    val links: LockerEntryLinks,
    val developer: LockerEntryDeveloper,
    @SerialName("hardware_platforms") val hardwarePlatforms: List<LockerEntryPlatform>,
    val compatibility: LockerEntryCompatibility,
    val companions: Map<String, LockerEntryCompanionApp?>,
    val pbw: LockerEntryPBW? = null
)

@Serializable
data class LockerEntryLinks(
    val remove: String,
    val href: String,
    val share: String
)

@Serializable
data class LockerEntryDeveloper(
    val id: String,
    val name: String,
    @SerialName("contact_email") val contactEmail: String
)

@Serializable
data class LockerEntryPlatform(
    @SerialName("sdk_version") val sdkVersion: String,
    @SerialName("pebble_process_info_flags") val pebbleProcessInfoFlags: Int,
    val name: String,
    val description: String,
    val images: LockerEntryPlatformImages
)

@Serializable
data class LockerEntryPlatformImages(
    val icon: String,
    val list: String,
    val screenshot: String
)

@Serializable
data class LockerEntryCompatibility(
    val ios: LockerEntryCompatibilityPhonePlatformDetails,
    val android: LockerEntryCompatibilityPhonePlatformDetails,
    val aplite: LockerEntryCompatibilityWatchPlatformDetails,
    val basalt: LockerEntryCompatibilityWatchPlatformDetails,
    val chalk: LockerEntryCompatibilityWatchPlatformDetails,
    val diorite: LockerEntryCompatibilityWatchPlatformDetails,
    val emery: LockerEntryCompatibilityWatchPlatformDetails
)

@Serializable
data class LockerEntryCompatibilityPhonePlatformDetails(
    val supported: Boolean,
    @SerialName("min_js_version") val minJsVersion: Int? = null
)

@Serializable
data class LockerEntryCompatibilityWatchPlatformDetails(
    val supported: Boolean,
    val firmware: LockerEntryFirmwareVersion
)

@Serializable
data class LockerEntryFirmwareVersion(
    val major: Int,
    val minor: Int? = null,
    val patch: Int? = null
)

@Serializable
data class LockerEntryCompanionApp(
    val id: Int,
    val icon: String,
    val name: String,
    val url: String,
    val required: Boolean,
    @SerialName("pebblekit_version") val pebblekitVersion: String
)

@Serializable
data class LockerEntryPBW(
    val file: String,
    @SerialName("icon_resource_id") val iconResourceId: Int,
    @SerialName("release_id") val releaseId: String
)

fun LockerEntry.toEntity(): SyncedLockerEntry {
    return SyncedLockerEntry(
        id = id,
        uuid = uuid,
        version = version ?: "",
        title = title,
        type = type,
        hearts = hearts,
        developerId = developer.id,
        developerName = developer.name,
        configurable = isConfigurable,
        timelineEnabled = isTimelineEnabled,
        removeLink = links.remove,
        shareLink = links.share,
        pbwLink = pbw?.file ?: error("PBW is null"),
        pbwReleaseId = pbw.releaseId,
        pbwIconResourceId = pbw.iconResourceId,
        nextSyncAction = if (type == "watchface") NextSyncAction.Ignore else NextSyncAction.Upload,
        order = -1,
        lastOpened = null,
        userToken = userToken
    )
}

fun LockerEntryPlatform.toEntity(lockerEntryId: String): SyncedLockerEntryPlatform {
    return SyncedLockerEntryPlatform(
        platformEntryId = 0,
        lockerEntryId = lockerEntryId,
        sdkVersion = sdkVersion,
        processInfoFlags = pebbleProcessInfoFlags,
        name = name,
        description = description,
        SyncedLockerEntryPlatformImages(
            icon = images.icon,
            list = images.list,
            screenshot = images.screenshot
        )
    )
}