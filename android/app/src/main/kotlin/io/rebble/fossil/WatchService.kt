package io.rebble.fossil

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import io.flutter.Log
import io.rebble.fossil.bluetooth.BlueCommon
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.services.notification.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus

@ExperimentalUnsignedTypes
class WatchService : LifecycleService() {
    private lateinit var coroutineScope: CoroutineScope

    private val pBinder = ProtBinder()
    private val logTag: String = "FossilWatchService"
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private lateinit var protocolHandler: ProtocolHandler
    private lateinit var blueCommon: BlueCommon
    lateinit var notificationService: NotificationService
        private set

    private val mainNotifBuilder = NotificationCompat.Builder(this, "device_status")
            .setContentTitle("Disconnected")
            .setSmallIcon(R.drawable.ic_notification_disconnected)

    inner class ProtBinder : Binder() {
        fun getService(): WatchService {
            return this@WatchService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return pBinder
    }


    @ExperimentalStdlibApi
    override fun onCreate() {
        val injectionComponent = (applicationContext as FossilApplication).component

        coroutineScope = lifecycleScope + injectionComponent.createExceptionHandler()
        blueCommon = injectionComponent.createBlueCommon()
        notificationService = injectionComponent.createNotificationService()
        protocolHandler = injectionComponent.createProtocolHandler()

        super.onCreate()

        // TODO: BLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Requires location permission for bluetooth LE", Toast.LENGTH_LONG).show()
                stopSelf()
                return
            }
        }

        if (!bluetoothAdapter.isEnabled) {
            Log.w(logTag, "Bluetooth - Not enabled")
        }

        startIO()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForeground(1, mainNotifBuilder.build())
        return START_STICKY
    }

    private fun setNotification(connected: Boolean) {
        mainNotifBuilder
                .setContentTitle(if (connected) "Connected to device" else "Disconnected")
                .setContentText(if (connected) blueCommon.driver!!.getTarget()?.name else null)
                .setSmallIcon(if (connected) R.drawable.ic_notification_connected else R.drawable.ic_notification_disconnected)
                .setPriority(if (connected) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_DEFAULT)
        NotificationManagerCompat.from(this).notify(1, mainNotifBuilder.build())
    }

    private fun startIO() {
        blueCommon.setOnConnectionChange { connected -> setNotification(connected) }
    }
}
