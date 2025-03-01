package io.rebble.cobble.shared

import android.content.Context
import io.rebble.libpebblecommon.packets.PhoneAppVersion

class AndroidPlatformContext(
    val applicationContext: Context
) : PlatformContext {
    override val osType: PhoneAppVersion.OSType = PhoneAppVersion.OSType.Android
}