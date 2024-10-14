package io.rebble.cobble.shared

import io.rebble.libpebblecommon.packets.PhoneAppVersion

class IOSPlatformContext : PlatformContext {
    override val osType: PhoneAppVersion.OSType = PhoneAppVersion.OSType.IOS
}