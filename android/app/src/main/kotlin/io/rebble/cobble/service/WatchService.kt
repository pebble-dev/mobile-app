package io.rebble.cobble.service

import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import io.rebble.cobble.*
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import io.rebble.cobble.handlers.CobbleHandler
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.services.notification.NotificationService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import timber.log.Timber
import javax.inject.Provider

@OptIn(ExperimentalCoroutinesApi::class)
class WatchService : LifecycleService() {
    lateinit var coroutineScope: CoroutineScope
    lateinit var watchConnectionScope: CoroutineScope

    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private lateinit var protocolHandler: ProtocolHandler
    private lateinit var connectionLooper: ConnectionLooper
    lateinit var notificationService: NotificationService
        private set

    private lateinit var mainNotifBuilder: NotificationCompat.Builder


    override fun onCreate() {
        mainNotifBuilder = createBaseNotificationBuilder(NOTIFICATION_CHANNEL_WATCH_CONNECTING)
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

        super.onCreate()

        if (!bluetoothAdapter.isEnabled) {
            Timber.w("Bluetooth - Not enabled")
        }

        startNotificationLoop()
        startHandlersLoop(serviceComponent.getMessageHandlersProvider())
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
                    is ConnectionState.WaitingForBluetoothToEnable -> {
                        icon = R.drawable.ic_notification_disconnected
                        titleText = getString(R.string.bluetooth_off)
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

                mainNotifBuilder = createBaseNotificationBuilder(channel)
                        .setContentTitle(titleText)
                        .setContentText(deviceName)
                        .setSmallIcon(icon)

                startForeground(1, mainNotifBuilder.build())
            }
        }
    }

    private fun createBaseNotificationBuilder(channel: String): NotificationCompat.Builder {
        val mainActivityIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        return NotificationCompat
                .Builder(this@WatchService, channel)
                .setContentIntent(mainActivityIntent)
    }

    private fun startHandlersLoop(handlers: Provider<Set<CobbleHandler>>) {
        coroutineScope.launch {
            connectionLooper.connectionState
                    .filterIsInstance<ConnectionState.Connected>()
                    .collect {
                        watchConnectionScope = connectionLooper
                                .getWatchConnectedScope(Dispatchers.Main.immediate)
                        handlers.get()
                    }
        }
    }
}
