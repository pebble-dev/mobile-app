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
import androidx.lifecycle.Lifecycle
import io.flutter.plugin.common.BinaryMessenger
import io.rebble.cobble.MainActivity
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.datasources.PermissionChangeBus
import io.rebble.cobble.notifications.NotificationListener
import io.rebble.cobble.pigeons.NumberWrapper
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.pigeons.toMapExt
import io.rebble.cobble.util.asFlow
import io.rebble.cobble.util.launchPigeonResult
import io.rebble.cobble.util.registerAsyncPigeonCallback
import io.rebble.cobble.util.voidResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class PermissionControlFlutterBridge @Inject constructor(
        private val activity: MainActivity,
        private val activityLifecycle: Lifecycle,
        private val coroutineScope: CoroutineScope,
        bridgeLifecycleController: BridgeLifecycleController
) : FlutterBridge, Pigeons.PermissionControl {
    init {
        bridgeLifecycleController.setupControl(Pigeons.PermissionControl::setup, this)
    }
    private suspend fun requestNotificationAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val companionDeviceManager: CompanionDeviceManager = activity.getSystemService()!!

            val lifecycleFlow = activityLifecycle.asFlow()

            coroutineScope {
                val waitUntilPaused = launch {
                    lifecycleFlow.first { it == Lifecycle.State.STARTED }
                }

                companionDeviceManager.requestNotificationAccess(ComponentName(activity, NotificationListener::class.java))

                // Wait until dialog appears - activity pauses
                waitUntilPaused.join()
                // Wait until user dialog disappears - activity resumes
                lifecycleFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            }
        } else {
            val resultCompletable = CompletableDeferred<Unit>()


            activity.activityResultCallbacks[REQUEST_CODE_NOTIFICATIONS] = { _, _ ->
                resultCompletable.complete(Unit)
            }

            activity.startActivityForResult(
                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"),
                    REQUEST_CODE_NOTIFICATIONS
            )

            resultCompletable.await()
        }

        PermissionChangeBus.trigger()
    }

    @SuppressLint("BatteryLife")
    private suspend fun requestBatteryExclusion() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        val resultCompletable = CompletableDeferred<Unit>()


        activity.activityResultCallbacks[REQUEST_CODE_BATTERY] = { _, _ ->
            resultCompletable.complete(Unit)
        }

        activity.startActivityForResult(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${activity.packageName}")
                },
                REQUEST_CODE_BATTERY
        )

        resultCompletable.await()
    }

    private suspend fun openPermissionSettings() {
        val resultCompletable = CompletableDeferred<Unit>()


        activity.activityResultCallbacks[REQUEST_CODE_SETTINGS] = { _, _ ->
            resultCompletable.complete(Unit)
        }

        activity.startActivityForResult(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${activity.packageName}")
                },
                REQUEST_CODE_SETTINGS
        )

        resultCompletable.await()
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

    override fun requestLocationPermission(result: Pigeons.Result<Pigeons.NumberWrapper>?) {
        coroutineScope.launchPigeonResult(result!!, coroutineScope.coroutineContext) {
            requestPermission(
                    REQUEST_CODE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    override fun requestCalendarPermission(result: Pigeons.Result<Pigeons.NumberWrapper>?) {
        coroutineScope.launchPigeonResult(result!!, coroutineScope.coroutineContext) {
            requestPermission(
                    REQUEST_CODE_CALENDAR,
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
            )
        }
    }

    override fun requestNotificationAccess(result: Pigeons.Result<Void?>?) {
        coroutineScope.launchPigeonResult(result!!, coroutineScope.coroutineContext) {
            requestNotificationAccess()

            null
        }
    }

    override fun requestBatteryExclusion(result: Pigeons.Result<Void?>?) {
        coroutineScope.launchPigeonResult(result!!, coroutineScope.coroutineContext) {
            requestBatteryExclusion()

            null
        }
    }

    override fun requestBluetoothPermissions(result: Pigeons.Result<Pigeons.NumberWrapper>?) {
        coroutineScope.launchPigeonResult(result!!, coroutineScope.coroutineContext) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermission(
                        REQUEST_CODE_BT,
                        Manifest.permission.BLUETOOTH_ADVERTISE,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                )
            } else {
                return@launchPigeonResult NumberWrapper(0)
            }
        }
    }

    override fun openPermissionSettings(result: Pigeons.Result<Void?>?) {
        coroutineScope.launchPigeonResult(result!!, coroutineScope.coroutineContext) {
            openPermissionSettings()

            null
        }
    }
}

private const val REQUEST_CODE_LOCATION = 123
private const val REQUEST_CODE_CALENDAR = 124
private const val REQUEST_CODE_NOTIFICATIONS = 125
private const val REQUEST_CODE_BATTERY = 126
private const val REQUEST_CODE_SETTINGS = 127
private const val REQUEST_CODE_BT = 128