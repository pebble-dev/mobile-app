package io.rebble.cobble.service

import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.MainActivity
import io.rebble.cobble.R
import io.rebble.cobble.bluetooth.BluetoothPebbleDevice
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.EmulatedPebbleDevice
import io.rebble.cobble.shared.domain.calendar.CalendarSync
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.util.NotificationId
import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class WatchService : LifecycleService() {
    lateinit var coroutineScope: CoroutineScope
    lateinit var watchConnectionScope: CoroutineScope

    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private lateinit var protocolHandler: ProtocolHandler
    private lateinit var connectionLooper: ConnectionLooper

    private lateinit var calendarSync: CalendarSync

    private lateinit var mainNotifBuilder: NotificationCompat.Builder

    override fun onCreate() {
        mainNotifBuilder =
            createBaseNotificationBuilder(NotificationId.NOTIFICATION_CHANNEL_WATCH_CONNECTING)
                .setContentTitle("Waiting to connect")
                .setContentText(null)
                .setSmallIcon(R.drawable.ic_notification_disconnected)
        startForeground(NotificationId.WATCH_CONNECTION, mainNotifBuilder.build())

        val injectionComponent = (applicationContext as CobbleApplication).component

        coroutineScope = lifecycleScope + injectionComponent.createExceptionHandler()
        connectionLooper = injectionComponent.createConnectionLooper()

        calendarSync = injectionComponent.createKMPCalendarSync()

        super.onCreate()

        if (!bluetoothAdapter.isEnabled) {
            Timber.w("Bluetooth - Not enabled")
        }

        startNotificationLoop()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
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
                    is ConnectionState.Negotiating,
                    is ConnectionState.WaitingForReconnect -> {
                        icon = R.drawable.ic_notification_disconnected
                        titleText = "Connecting"
                        deviceName = null
                        channel = NotificationId.NOTIFICATION_CHANNEL_WATCH_CONNECTING
                    }

                    is ConnectionState.WaitingForTransport -> {
                        icon = R.drawable.ic_notification_disconnected
                        titleText = getString(R.string.bluetooth_off)
                        deviceName = null
                        channel = NotificationId.NOTIFICATION_CHANNEL_WATCH_CONNECTING
                    }

                    is ConnectionState.Connected -> {
                        icon = R.drawable.ic_notification_connected
                        titleText = "Connected to device"
                        deviceName =
                            if (it.watch is EmulatedPebbleDevice) {
                                "[EMU] ${it.watch.address}"
                            } else if (it.watch is BluetoothPebbleDevice) {
                                (it.watch as BluetoothPebbleDevice).bluetoothDevice.name!!
                            } else {
                                it.watch.address
                            }
                        channel = NotificationId.NOTIFICATION_CHANNEL_WATCH_CONNECTED
                    }

                    is ConnectionState.RecoveryMode -> {
                        icon = R.drawable.ic_notification_connected
                        titleText = "Connected to device (Recovery Mode)"
                        deviceName =
                            if (it.watch is EmulatedPebbleDevice) {
                                "[EMU] ${it.watch.address}"
                            } else if (it.watch is BluetoothPebbleDevice) {
                                (it.watch as BluetoothPebbleDevice).bluetoothDevice.name!!
                            } else {
                                it.watch.address
                            }
                        channel = NotificationId.NOTIFICATION_CHANNEL_WATCH_CONNECTED
                    }
                    else -> error("Unhandled connection state")
                }

                Timber.d("Notification Title Text %s", titleText)

                mainNotifBuilder =
                    createBaseNotificationBuilder(channel)
                        .setContentTitle(titleText)
                        .setContentText(deviceName)
                        .setSmallIcon(icon)

                startForeground(1, mainNotifBuilder.build())
            }
        }
    }

    private fun createBaseNotificationBuilder(channel: String): NotificationCompat.Builder {
        val mainActivityIntent =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            )

        return NotificationCompat
            .Builder(this@WatchService, channel)
            .setContentIntent(mainActivityIntent)
    }
}