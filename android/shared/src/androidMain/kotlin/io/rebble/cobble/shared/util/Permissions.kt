package io.rebble.cobble.shared.util

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

fun Context.hasCallsPermission() =
    checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == android.content.pm.PackageManager.PERMISSION_GRANTED

fun Context.hasContactsPermission() =
    checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED

fun Context.hasNotificationPostingPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}