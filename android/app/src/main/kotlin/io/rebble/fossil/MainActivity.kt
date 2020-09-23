package io.rebble.fossil

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import io.flutter.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import io.rebble.fossil.bluetooth.BluePebbleDevice
import io.rebble.libpebblecommon.blobdb.PushNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import kotlin.system.exitProcess

@ExperimentalUnsignedTypes
class MainActivity : FlutterActivity() {
    private lateinit var coroutineScope: CoroutineScope

    var watchService: WatchService? = null
    var isBound = false
    var bootIntentCallback: ((Boolean) -> Unit)? = null

    private val watchServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            val binder = service as WatchService.ProtBinder
            watchService = binder.getService()
            Log.d("Binding", "Done")
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            10 -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Requires location permission for bluetooth LE", Toast.LENGTH_LONG).show()
                    exitProcess(1)
                } else {
                    if (!isBound) {
                        val intent = Intent(this, WatchService::class.java)
                        bindService(intent, watchServiceConnection, Context.BIND_AUTO_CREATE)
                    }
                }
            }
        }
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            val data = intent.data
            if (data?.scheme == "pebble") {
                when (data.host) {
                    "custom-boot-config-url" -> {
                        val prefs = getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
                        try {
                            val boot = URI.create(data.pathSegments[0])

                            val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE -> {
                                        prefs.edit().putString("flutter.boot", boot.toString()).apply()
                                        Toast.makeText(context, "Updated boot URL: $boot", Toast.LENGTH_LONG).show()
                                        bootIntentCallback?.invoke(true)
                                    }
                                    DialogInterface.BUTTON_NEGATIVE -> {
                                        Toast.makeText(context, "Cancelled boot URL change", Toast.LENGTH_SHORT).show()
                                        bootIntentCallback?.invoke(false)
                                    }
                                }
                            }

                            AlertDialog.Builder(context)
                                    .setTitle(R.string.bootUrlWarningTitle)
                                    .setMessage(getString(R.string.bootUrlWarningBody, boot.toString()))
                                    .setPositiveButton("Allow", dialogClickListener)
                                    .setNegativeButton("Deny", dialogClickListener).show()
                        } catch (e: IllegalArgumentException) {
                            Toast.makeText(this, "Boot URL not updated, was invalid", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("device_status", "Device Status", importance)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat: String = Settings.Secure.getString(contentResolver,
                "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val injectionComponent = (applicationContext as FossilApplication).component

        coroutineScope = lifecycleScope + injectionComponent.createExceptionHandler()

        super.onCreate(savedInstanceState)
        GeneratedPluginRegistrant.registerWith(this.flutterEngine!!)

        handleIntent(intent)

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("This app needs location access")
                builder.setMessage("Please grant location access in order to scan for Pebbles.")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    ActivityCompat.requestPermissions(
                            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            10
                    )
                }
                builder.show()
            }
        }

        if (!isNotificationServiceEnabled()) {
            //TODO: Save their choice and stop nagging if user doesn't want this
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Allow notification reading")
            alertDialogBuilder.setMessage("To see notifications on your watch, you need to give the app access to read incoming notifications on your device")

            alertDialogBuilder.setPositiveButton("Allow") { _, _ -> startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) }
            alertDialogBuilder.setNegativeButton("Deny") { _, _ -> Toast.makeText(applicationContext, "Running without showing notifications", Toast.LENGTH_LONG).show() }
            alertDialogBuilder.create().show()
        }

        val intent = Intent(this, WatchService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        }
        bindService(intent, watchServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private var deviceList: MutableList<BluePebbleDevice> = mutableListOf()

    @OptIn(ExperimentalStdlibApi::class)
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        val flutter = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "io.rebble.fossil/protocol")
        val packetIO = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "io.rebble.fossil/packetIO")
        val bootWaiter = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "io.rebble.fossil/bootWaiter")
        val notificationTester = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "io.rebble.fossil/notificationTest")

        flutter.setMethodCallHandler { call, result ->
            when (call.method) {
                "isConnected" -> result.success(watchService?.isConnected())
                "targetPebbleAddr" -> result.success(watchService?.targetPebble(call.arguments as Long))
                "scanDevices" -> {
                    deviceList.clear()
                    watchService?.scanDevices({ el ->
                        val oldIn = deviceList.indexOfFirst({p -> p.bluetoothDevice.address == el.bluetoothDevice.address})
                        if (oldIn < 0 && el.leMeta?.serialNumber != "??") {
                            deviceList.add(el)
                            Log.d("--DEBUG--", el.leMeta.toString())
                            val json = JSONObject()
                                    .put("name", el.bluetoothDevice.name)
                                    .put("address", el.bluetoothDevice.address)
                            if (el.leMeta?.major != null) {
                                json.put("version", "${el.leMeta.major}.${el.leMeta.minor}.${el.leMeta.patch}")
                            }
                            if (el.leMeta?.serialNumber != null) {
                                json.put("serialNumber", el.leMeta.serialNumber)
                            }
                            if (el.leMeta?.color != null) {
                                json.put("color", el.leMeta.color)
                            }
                            if (el.leMeta?.runningPRF != null) {
                                json.put("runningPRF", el.leMeta.runningPRF)
                            }
                            if (el.leMeta?.firstUse != null) {
                                json.put("firstUse", el.leMeta.firstUse)
                            }
                            flutter.invokeMethod("addPairScanResult", json.toString())
                        }
                    }) {
                        flutter.invokeMethod("finishPairScan", null)
                    }
                    result.success(null)
                }
            }
        }

        packetIO.setMethodCallHandler { call, result ->
            when (call.method) {
                "send" ->
                    watchService?.sendDevPacket(call.arguments as ByteArray)
            }
        }

        bootWaiter.setMethodCallHandler { call, result ->
            when (call.method) {
                "waitForBoot" ->
                    bootIntentCallback = { success ->
                        result.success(success)
                        bootIntentCallback = null
                    }
            }
        }

        notificationTester.setMethodCallHandler { call, result ->
            when (call.method) {
                "sendTestNotification" -> {
                    coroutineScope.launch {
                        watchService?.notificationService?.send(
                                PushNotification(
                                        "Test Notification"

                                )
                        )

                        println("Notification sent")
                    }
                }
            }
        }
    }

}
