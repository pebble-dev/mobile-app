package io.rebble.cobble.util

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService

fun Context.hasNotificationAccessPermission(): Boolean {
    return NotificationManagerCompat.getEnabledListenerPackages(this)
            .contains(this.packageName)
}

fun Context.hasBatteryExclusionPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return true
    }

    val powerManager: PowerManager = getSystemService()!!
    return powerManager.isIgnoringBatteryOptimizations(packageName)
}