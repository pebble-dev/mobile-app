package io.rebble.cobble.bridges.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.pigeons.BooleanWrapper
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.shared.util.hasBatteryExclusionPermission
import io.rebble.cobble.shared.util.hasCallsPermission
import io.rebble.cobble.shared.util.hasContactsPermission
import io.rebble.cobble.shared.util.hasNotificationAccessPermission
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
        return checkPermission(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
        )
    }

    override fun hasNotificationAccess(): Pigeons.BooleanWrapper {
        return BooleanWrapper(
                context.hasNotificationAccessPermission()
        )
    }

    override fun hasBatteryExclusionEnabled(): Pigeons.BooleanWrapper {
        return BooleanWrapper(context.hasBatteryExclusionPermission())
    }

    override fun hasCallsPermissions(): Pigeons.BooleanWrapper {
        return BooleanWrapper(context.hasCallsPermission() && context.hasContactsPermission())
    }

    private fun checkPermission(vararg permission: String) = BooleanWrapper(
            permission.all {
                ContextCompat.checkSelfPermission(
                        context,
                        it
                ) == PackageManager.PERMISSION_GRANTED
            }
    )
}