package io.rebble.cobble.service

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.NOTIFICATION_CHANNEL_WATCH_CONNECTED
import io.rebble.cobble.NOTIFICATION_CHANNEL_WATCH_CONNECTING
import io.rebble.cobble.R
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.services.notification.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class WatchService : LifecycleService() {
    lateinit var coroutineScope: CoroutineScope

    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private lateinit var protocolHandler: ProtocolHandler
    private lateinit var connectionLooper: ConnectionLooper
    lateinit var notificationService: NotificationService
        private set

    private lateinit var mainNotifBuilder: NotificationCompat.Builder


    override fun onCreate() {
        mainNotifBuilder = NotificationCompat
                .Builder(this@WatchService, NOTIFICATION_CHANNEL_WATCH_CONNECTING)
                .setSmallIcon(R.drawable.ic_notification_disconnected)
                .setContentTitle("Waiting to connect")
                .setContentText(null)
                .setSmallIcon(R.drawable.ic_notification_disconnected)
        startForeground(1, mainNotifBuilder.build())

        val injectionComponent = (applicationContext as CobbleApplication).component

        coroutineScope = lifecycleScope + injectionComponent.createExceptionHandler()
        notificationService = injectionComponent.createNotificationService()
        protocolHandler = injectionComponent.createProtocolHandler()
        connectionLooper = injectionComponent.createConnectionLooper()

        val serviceComponent = injectionComponent.createServiceSubcomponentFactory()
                .create(this)

        serviceComponent.initAllMessageHandlers()

        super.onCreate()

        if (!bluetoothAdapter.isEnabled) {
            Timber.w("Bluetooth - Not enabled")
        }

        startNotificationLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun startNotificationLoop() {
        coroutineScope.launch {
            Timber.d("Notification Loop start")
            connectionLooper.connectionState.collect {
                Timber.d("Update notification state %s", it)

                @DrawableRes val icon: Int
                val titleText: String?
                val deviceName: String?
                val channel: String

                when (it) {
                    is ConnectionState.Disconnected -> {
                        // There is no disconnected notification
                        // service will be stopped
                        return@collect
                    }
                    is ConnectionState.Connecting,
                    is ConnectionState.WaitingForReconnect -> {
                        icon = R.drawable.ic_notification_disconnected
                        titleText = "Connecting"
                        deviceName = null
                        channel = NOTIFICATION_CHANNEL_WATCH_CONNECTING
                    }
                    is ConnectionState.Connected -> {
                        icon = R.drawable.ic_notification_connected
                        titleText = "Connected to device"
                        deviceName = it.watch.name
                        channel = NOTIFICATION_CHANNEL_WATCH_CONNECTED
                    }
                }

                Timber.d("Notification Title Text %s", titleText)

                mainNotifBuilder = NotificationCompat
                        .Builder(this@WatchService, channel)
                        .setSmallIcon(R.drawable.ic_notification_disconnected)
                        .setContentTitle(titleText)
                        .setContentText(deviceName)
                        .setSmallIcon(icon)

                startForeground(1, mainNotifBuilder.build())
            }
        }
    }
}
