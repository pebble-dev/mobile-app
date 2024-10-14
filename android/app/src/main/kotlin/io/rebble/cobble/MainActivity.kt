package io.rebble.cobble

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.text.TextUtils
import android.widget.Toast
import androidx.collection.ArrayMap
import androidx.lifecycle.lifecycleScope
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.datasources.PermissionChangeBus
import io.rebble.cobble.notifications.NotificationListener
import io.rebble.cobble.service.CompanionDeviceService
import io.rebble.cobble.service.InCallService
import io.rebble.cobble.util.hasNotificationAccessPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import java.net.URI

@OptIn(ExperimentalUnsignedTypes::class)
class MainActivity : FlutterActivity() {
    lateinit var coroutineScope: CoroutineScope
    private lateinit var flutterBridges: Set<FlutterBridge>

    var bootIntentCallback: ((Boolean) -> Unit)? = null

    /**
     * Parameters: code, state, error
     */
    var oauthIntentCallback: ((String?, String?, String?) -> Unit)? = null
    var intentCallback: ((Intent) -> Unit)? = null

    val activityResultCallbacks = ArrayMap<Int, (resultCode: Int, data: Intent?) -> Unit>()
    val activityPermissionCallbacks = ArrayMap<
            Int,
            (permissions: Array<String>, grantResults: IntArray) -> Unit>()

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        activityPermissionCallbacks[requestCode]?.invoke(permissions, grantResults)
        PermissionChangeBus.trigger()
    }

    private fun handleIntent(intent: Intent) {
        intentCallback?.invoke(intent)

        if (intent.action == Intent.ACTION_VIEW) {
            val data = intent.data
            when (data?.scheme) {
                "pebble" -> {
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

                "rebble" -> {
                    when (data.host) {
                        "auth_complete" -> {
                            val code = data.getQueryParameter("code")
                            val state = data.getQueryParameter("state")
                            val error = data.getQueryParameter("error")
                            oauthIntentCallback?.invoke(code, state, error)
                        }
                    }
                }
            }
        }
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

        startAdditionalServices()

        handleIntent(intent)
    }

    /**
     * Start the CompanionDeviceService and InCallService
     */
    private fun startAdditionalServices() {
        // The CompanionDeviceService is available but we want tiramisu APIs so limit it to that
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val companionDeviceServiceIntent = Intent(this, CompanionDeviceService::class.java)
            startService(companionDeviceServiceIntent)
        }

        val inCallServiceIntent = Intent(this, InCallService::class.java)
        startService(inCallServiceIntent)


        if (context.hasNotificationAccessPermission()) {
            NotificationListenerService.requestRebind(
                    NotificationListener.getComponentName(context)
            )
        }
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
