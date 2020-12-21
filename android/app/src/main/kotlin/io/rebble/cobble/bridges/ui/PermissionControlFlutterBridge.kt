package io.rebble.cobble.bridges.ui

import android.Manifest
import android.annotation.SuppressLint
import android.companion.CompanionDeviceManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.getSystemService
import io.flutter.plugin.common.BinaryMessenger
import io.rebble.cobble.MainActivity
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.notifications.NotificationListener
import io.rebble.cobble.pigeons.NumberWrapper
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.pigeons.toMapExt
import io.rebble.cobble.util.registerAsyncPigeonCallback
import io.rebble.cobble.util.voidResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class PermissionControlFlutterBridge @Inject constructor(
        private val activity: MainActivity,
        coroutineScope: CoroutineScope,
        binaryMessenger: BinaryMessenger
) : FlutterBridge {
    init {
        binaryMessenger.registerAsyncPigeonCallback(
                coroutineScope,
                "dev.flutter.pigeon.PermissionControl.requestLocationPermission"
        ) {
            requestPermission(
                    REQUEST_CODE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            ).toMapExt()
        }

        binaryMessenger.registerAsyncPigeonCallback(
                coroutineScope,
                "dev.flutter.pigeon.PermissionControl.requestCalendarPermission"
        ) {
            requestPermission(
                    REQUEST_CODE_CALENDAR,
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
            ).toMapExt()
        }

        binaryMessenger.registerAsyncPigeonCallback(
                coroutineScope,
                "dev.flutter.pigeon.PermissionControl.requestNotificationAccess"
        ) {
            requestNotificationAccess()

            voidResult
        }

        binaryMessenger.registerAsyncPigeonCallback(
                coroutineScope,
                "dev.flutter.pigeon.PermissionControl.requestBatteryExclusion"
        ) {
            requestBatteryExclusion()

            voidResult
        }

        binaryMessenger.registerAsyncPigeonCallback(
                coroutineScope,
                "dev.flutter.pigeon.PermissionControl.openPermissionSettings"
        ) {
            openPermissionSettings()

            voidResult
        }
    }

    private fun requestNotificationAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val companionDeviceManager: CompanionDeviceManager = activity.getSystemService()!!
            companionDeviceManager.requestNotificationAccess(ComponentName(activity, NotificationListener::class.java))
        } else {
            activity.startActivity(
                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            )
        }
    }

    @SuppressLint("BatteryLife")
    private fun requestBatteryExclusion() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        activity.startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
        )
    }

    private fun openPermissionSettings() {
        activity.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
        )
    }

    private suspend fun requestPermission(
            requestCode: Int,
            vararg permissions: String
    ): Pigeons.NumberWrapper {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return NumberWrapper(0)
        }

        val completableDeferred = CompletableDeferred<Int>()

        activity.activityPermissionCallbacks[requestCode] = { _, results: IntArray ->
            val granted = results.isNotEmpty() && results[0] == PackageManager.PERMISSION_GRANTED

            completableDeferred.complete(if (granted) {
                0
            } else {
                if (permissions.any { activity.shouldShowRequestPermissionRationale(it) }) {
                    1
                } else {
                    2
                }
            })
        }

        activity.requestPermissions(permissions, requestCode)

        val result = completableDeferred.await()
        return NumberWrapper(result)
    }
}

private const val REQUEST_CODE_LOCATION = 123
private const val REQUEST_CODE_CALENDAR = 124