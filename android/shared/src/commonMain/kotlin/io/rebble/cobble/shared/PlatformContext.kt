package io.rebble.cobble.shared

import io.rebble.libpebblecommon.packets.PhoneAppVersion

interface PlatformContext {
    val osType: PhoneAppVersion.OSType
}