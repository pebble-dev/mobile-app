package io.rebble.cobble.shared.domain.common

import io.rebble.libpebblecommon.packets.WatchVersion
import kotlinx.coroutines.flow.MutableStateFlow

open class PebbleDevice(
    metadata: WatchVersion.WatchVersionResponse?,
    val address: String
) {
    val metadata: MutableStateFlow<WatchVersion.WatchVersionResponse?> = MutableStateFlow(metadata)

    override fun toString(): String = "< PebbleDevice address=$address >"
}