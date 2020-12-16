package io.rebble.cobble

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.collection.ArrayMap
import androidx.lifecycle.lifecycleScope
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.rebble.cobble.bridges.FlutterBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import java.net.URI

@OptIn(ExperimentalUnsignedTypes::class)
class MainActivity : FlutterActivity() {
    lateinit var coroutineScope: CoroutineScope
    private lateinit var flutterBridges: Set<FlutterBridge>

    var isBound = false
    var bootIntentCallback: ((Boolean) -> Unit)? = null

    val activityResultCallbacks = ArrayMap<Int, (resultCode: Int, data: Intent?) -> Unit>()
    val activityPermissionCallbacks = ArrayMap<
            Int,
            (permissions: Array<String>, grantResults: IntArray) -> Unit>()

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        activityPermissionCallbacks[requestCode]?.invoke(permissions, grantResults)
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

        // Bridges need to be created after super.onCreate() to ensure
        // flutter stuff is ready
        flutterBridges = activityComponent.createCommonBridges() +
                activityComponent.createUiBridges()

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        activityResultCallbacks[requestCode]?.invoke(resultCode, data)
    }

    public override fun getFlutterEngine(): FlutterEngine? {
        return super.getFlutterEngine()
    }
}
