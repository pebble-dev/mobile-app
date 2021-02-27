package io.rebble.cobble.notifications

import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import io.rebble.cobble.datasources.FlutterPreferences
import io.rebble.libpebblecommon.services.notification.NotificationService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import timber.log.Timber

class NotificationListener : NotificationListenerService() {
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var connectionLooper: ConnectionLooper
    private lateinit var flutterPreferences: FlutterPreferences

    private var isListening = false
    private var areNotificationsEnabled = true

    private var notifStates: MutableMap<NotificationKey, ParsedNotification> = mutableMapOf()

    private lateinit var notificationService: NotificationService

    override fun onCreate() {
        val injectionComponent = (applicationContext as CobbleApplication).component

        coroutineScope = CoroutineScope(
                SupervisorJob() + injectionComponent.createExceptionHandler()
        )

        connectionLooper = injectionComponent.createConnectionLooper()
        notificationService = injectionComponent.createNotificationService()
        flutterPreferences = injectionComponent.createFlutterPreferences()

        super.onCreate()
        _isActive.value = true
    }

    override fun onDestroy() {
        _isActive.value = false
        super.onDestroy()

        coroutineScope.cancel()
    }

    override fun onListenerConnected() {
        isListening = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            unbindOnWatchDisconnect()
        }

        controlListenerHints()
        observeNotificationToggle()
    }

    override fun onListenerDisconnected() {
        isListening = false
    }

    private fun sendNotif(parsedNotification: ParsedNotification) {
        coroutineScope.launch {
            notificationService.send(parsedNotification.toBluetoothPacket())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (isListening && areNotificationsEnabled) {
            if (sbn == null) return
            if (sbn.packageName == applicationContext.packageName) return // Don't show a notification if it's us

            if (sbn.notification.extras.containsKey(NotificationCompat.EXTRA_MEDIA_SESSION)) {
                // Do not notify for media notifications
                return
            }

            val parsedNotification = sbn.parseData(applicationContext)
            val key = NotificationKey(sbn)

            // If the content is the exact same as it was before (and the notif isnt new / previously dismissed), ignore the new notif
            val existingNotification = notifStates.put(key, parsedNotification)
            if (existingNotification == parsedNotification) {
                return
            }

            sendNotif(parsedNotification)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (isListening) {
            Timber.d("Notification removed: ${sbn.packageName}")

            notifStates.remove(NotificationKey(sbn))
            //TODO: Dismissing on watch
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun unbindOnWatchDisconnect() {
        // It is a waste of resources to keep running notification listener in the background when
        // watch disconnects.

        // When watch disconnects, we call requestUnbind() to kill ourselves it and wait for
        // ServiceLifecycleControl to starts up back up when watch reconnects.

        coroutineScope.launch(Dispatchers.Main.immediate) {
            connectionLooper.connectionState.collect {
                if (it is ConnectionState.Disconnected) {
                    requestUnbind()
                }
            }
        }
    }

    private fun controlListenerHints() {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            combine(
                    flutterPreferences.mutePhoneNotificationSounds,
                    flutterPreferences.mutePhoneCallSounds
            ) {
                mutePhoneNotificationSounds, mutePhoneCallSounds ->

                val listenerHints = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    var hints = 0

                    if (mutePhoneNotificationSounds) {
                        hints = hints or HINT_HOST_DISABLE_NOTIFICATION_EFFECTS
                    }

                    if (mutePhoneCallSounds) {
                        hints = hints or HINT_HOST_DISABLE_CALL_EFFECTS
                    }

                    hints
                } else {
                    if (mutePhoneCallSounds || mutePhoneNotificationSounds) {
                        HINT_HOST_DISABLE_EFFECTS
                    } else {
                        0
                    }
                }

                requestListenerHints(listenerHints)
            }.collect()
        }
    }

    private fun observeNotificationToggle() {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            flutterPreferences.masterNotificationsToggle.collect {
                areNotificationsEnabled = it
            }
        }
    }

    companion object {
        private val _isActive = MutableStateFlow(false)
        val isActive: StateFlow<Boolean> by ::_isActive

        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, NotificationListener::class.java)
        }
    }
}