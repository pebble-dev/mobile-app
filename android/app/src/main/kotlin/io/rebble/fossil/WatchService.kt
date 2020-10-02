package io.rebble.fossil

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import io.flutter.Log
import io.rebble.fossil.bluetooth.ConnectionLooper
import io.rebble.fossil.bluetooth.ConnectionState
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.services.notification.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class WatchService : LifecycleService() {
    private lateinit var coroutineScope: CoroutineScope

    private val logTag: String = "FossilWatchService"
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private lateinit var protocolHandler: ProtocolHandler
    private lateinit var connectionLooper: ConnectionLooper
    lateinit var notificationService: NotificationService
        private set

    private val mainNotifBuilder = NotificationCompat.Builder(this, "device_status")
            .setContentTitle("Disconnected")
            .setSmallIcon(R.drawable.ic_notification_disconnected)


    @ExperimentalStdlibApi
    override fun onCreate() {
        val injectionComponent = (applicationContext as FossilApplication).component

        coroutineScope = lifecycleScope + injectionComponent.createExceptionHandler()
        notificationService = injectionComponent.createNotificationService()
        protocolHandler = injectionComponent.createProtocolHandler()
        connectionLooper = injectionComponent.createConnectionLooper()

        super.onCreate()

        if (!bluetoothAdapter.isEnabled) {
            Log.w(logTag, "Bluetooth - Not enabled")
        }

        startNotificationLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun startNotificationLoop() {
        coroutineScope.launch {
            connectionLooper.connectionState.collect {
                @DrawableRes val icon: Int
                val titleText: String?
                val deviceName: String?
                val notificationPriority: Int

                when (it) {
                    is ConnectionState.Disconnected -> {
                        icon = R.drawable.ic_notification_disconnected
                        titleText = "Disconnected"
                        deviceName = null
                        notificationPriority = NotificationCompat.PRIORITY_LOW
                    }
                    is ConnectionState.Connecting -> {
                        icon = R.drawable.ic_notification_disconnected
                        titleText = "Connecting"
                        deviceName = null
                        notificationPriority = NotificationCompat.PRIORITY_DEFAULT
                    }
                    is ConnectionState.Connected -> {
                        icon = R.drawable.ic_notification_connected
                        titleText = "Connected to device"
                        deviceName = it.watch.name
                        notificationPriority = NotificationCompat.PRIORITY_DEFAULT
                    }
                }

                mainNotifBuilder
                        .setContentTitle(titleText)
                        .setContentText(deviceName)
                        .setSmallIcon(icon)
                        .setPriority(notificationPriority)

                startForeground(1, mainNotifBuilder.build())
            }
        }
    }
}
