package io.rebble.cobble.shared.handlers

import io.rebble.cobble.shared.PlatformContext
import io.rebble.libpebblecommon.packets.PhoneAppVersion
import kotlinx.coroutines.flow.Flow

actual fun platformTimeChangedFlow(context: PlatformContext): Flow<Unit> {
    TODO("Not yet implemented")
}

actual fun getPlatformPebbleFlags(context: PlatformContext): Set<PhoneAppVersion.PlatformFlag> {
    TODO("Not yet implemented")
}