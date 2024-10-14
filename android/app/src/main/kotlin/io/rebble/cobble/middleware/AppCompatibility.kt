package io.rebble.cobble.middleware

import io.rebble.libpebblecommon.metadata.WatchType

fun getCompatibleAppVariants(watchType: WatchType): List<WatchType> {
    return when (watchType) {
        WatchType.APLITE -> listOf(WatchType.APLITE)
        WatchType.BASALT -> listOf(WatchType.BASALT, WatchType.APLITE)
        WatchType.CHALK -> listOf(WatchType.CHALK)
        WatchType.DIORITE -> listOf(WatchType.DIORITE, WatchType.APLITE)
        WatchType.EMERY -> listOf(
                WatchType.EMERY,
                WatchType.BASALT,
                WatchType.DIORITE,
                WatchType.APLITE
        )
    }
}

/**
 * @param availableAppVariants List of variants, from [PbwAppInfo.targetPlatforms]
 */
fun getBestVariant(watchType: WatchType, availableAppVariants: List<String>): WatchType? {
    val compatibleVariants = getCompatibleAppVariants(watchType)

    return compatibleVariants.firstOrNull { variant ->
        availableAppVariants.contains(variant.codename)
    }
}