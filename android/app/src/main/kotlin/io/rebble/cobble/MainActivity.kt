package io.rebble.cobble

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.collection.ArrayMap
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugins.GeneratedPluginRegistrant
import io.rebble.cobble.bridges.FlutterBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import java.net.URI
import kotlin.system.exitProcess

@OptIn(ExperimentalUnsignedTypes::class)
class MainActivity : FlutterActivity() {
    lateinit var coroutineScope: CoroutineScope
    private lateinit var flutterBridges: Set<FlutterBridge>

    var isBound = false
    var bootIntentCallback: ((Boolean) -> Unit)? = null

    val activityResultCallbacks = ArrayMap<Int, (resultCode: Int, data: Intent) -> Unit>()

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            10 -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Requires location permission for bluetooth LE", Toast.LENGTH_LONG).show()
                    exitProcess(1)
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

    private fun isNotificationServiceEnabled(): Boolean {
        try {
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
        } catch (e: NullPointerException) {
            return false
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val injectionComponent = (applicationContext as CobbleApplication).component
        val activityComponent = injectionComponent.createActivitySubcomponentFactory()
                .create(this)

        coroutineScope = lifecycleScope + injectionComponent.createExceptionHandler()

        super.onCreate(savedInstanceState)
        GeneratedPluginRegistrant.registerWith(this.flutterEngine!!)

        // Bridges need to be created after super.onCreate() to ensure
        // flutter stuff is ready
        flutterBridges = activityComponent.createFlutterBridges()

        handleIntent(intent)

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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        activityResultCallbacks[requestCode]?.invoke(resultCode, data)
    }

    public override fun getFlutterEngine(): FlutterEngine? {
        return super.getFlutterEngine()
    }
}
