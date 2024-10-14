package io.rebble.cobble.shared.util

import io.rebble.libpebblecommon.packets.WatchFirmwareVersion

data class RunningFirmwareVersionParsed(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val suffix: String
)

fun WatchFirmwareVersion.parsed(): RunningFirmwareVersionParsed {
    val pattern = Regex("[v]?([\\d]+)\\.([\\d]+)\\.?([\\d]*)[\\-]?([\\w\\-\\.]*)")
    val match = pattern.matchEntire(this.versionTag.get())
            ?: throw IllegalArgumentException("Invalid version tag: ${this.versionTag.get()}")
    val (major, minor, patch, suffix) = match.destructured
    return RunningFirmwareVersionParsed(
            major = major.toInt(),
            minor = minor.toInt(),
            patch = if (patch.isEmpty()) 0 else patch.toInt(),
            suffix = suffix
    )
}