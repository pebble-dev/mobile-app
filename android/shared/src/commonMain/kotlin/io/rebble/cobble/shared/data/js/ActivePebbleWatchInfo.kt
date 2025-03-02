package io.rebble.cobble.shared.data.js

import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.domain.common.PebbleWatchModel
import io.rebble.cobble.shared.util.parsed
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import kotlinx.serialization.Serializable

@Serializable
data class ActivePebbleWatchInfo(
    val platform: String,
    val model: String,
    val language: String,
    val firmware: FirmwareVersion
) {
    @Serializable
    data class FirmwareVersion(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val suffix: String
    )
}

fun ActivePebbleWatchInfo.Companion.fromDevice(device: PebbleDevice): ActivePebbleWatchInfo {
    val metadata = device.metadata.value ?: error("Device metadata is null")
    val modelId = device.modelId.value ?: -1
    val platform = WatchHardwarePlatform.fromProtocolNumber(metadata.running.hardwarePlatform.get())
    val color = PebbleWatchModel.fromProtocolNumber(modelId)
    val parsedFwVersion = metadata.running.parsed()
    return ActivePebbleWatchInfo(
        platform = platform?.watchType?.codename ?: "unknown",
        model = color.jsName,
        language = metadata.language.get(),
        firmware =
            ActivePebbleWatchInfo.FirmwareVersion(
                major = parsedFwVersion.major,
                minor = parsedFwVersion.minor,
                patch = parsedFwVersion.patch,
                suffix = parsedFwVersion.suffix
            )
    )
}