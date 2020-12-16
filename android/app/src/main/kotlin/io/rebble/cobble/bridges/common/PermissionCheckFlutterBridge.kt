package io.rebble.cobble.bridges.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.pigeons.BooleanWrapper
import io.rebble.cobble.pigeons.Pigeons
import javax.inject.Inject

class PermissionCheckFlutterBridge @Inject constructor(
        private val context: Context,
        bridgeLifecycleController: BridgeLifecycleController
) : Pigeons.PermissionCheck, FlutterBridge {
    init {
        bridgeLifecycleController.setupControl(Pigeons.PermissionCheck::setup, this)
    }

    override fun hasLocationPermission(): Pigeons.BooleanWrapper {
        return checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun hasCalendarPermission(): Pigeons.BooleanWrapper {
        return checkPermission(Manifest.permission.READ_CALENDAR)
    }

    override fun hasNotificationAccess(): Pigeons.BooleanWrapper {
        return BooleanWrapper(
                NotificationManagerCompat.getEnabledListenerPackages(context)
                        .contains(context.packageName)
        )
    }

    override fun hasBatteryExclusionEnabled(): Pigeons.BooleanWrapper {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return BooleanWrapper(true)
        }

        val powerManager: PowerManager = context.getSystemService()!!
        return BooleanWrapper(powerManager.isIgnoringBatteryOptimizations(context.packageName))
    }

    private fun checkPermission(permission: String) = BooleanWrapper(
            ContextCompat.checkSelfPermission(
                    context,
                    permission
            ) == PackageManager.PERMISSION_GRANTED
    )
}