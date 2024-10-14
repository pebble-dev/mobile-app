package io.rebble.cobble.shared

import io.rebble.libpebblecommon.packets.PhoneAppVersion

class AndroidPlatformContext(
        val applicationContext: android.content.Context
) : PlatformContext {
    override val osType: PhoneAppVersion.OSType = PhoneAppVersion.OSType.Android
}