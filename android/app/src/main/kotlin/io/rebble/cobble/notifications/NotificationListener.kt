package io.rebble.cobble.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.content.ComponentName
import android.content.Context
import android.icu.text.UnicodeSet
import android.os.Build
import android.os.UserHandle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bridges.background.NotificationsFlutterBridge
import io.rebble.cobble.data.NotificationAction
import io.rebble.cobble.data.NotificationMessage
import io.rebble.cobble.data.toNotificationGroup
import io.rebble.cobble.datasources.FlutterPreferences
import io.rebble.cobble.shared.datastore.KMPPrefs
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.libpebblecommon.packets.blobdb.BlobResponse
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import io.rebble.libpebblecommon.services.notification.NotificationService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber

class NotificationListener : NotificationListenerService() {
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var connectionLooper: ConnectionLooper
    private lateinit var flutterPreferences: FlutterPreferences
    private lateinit var notificationProcessor: NotificationProcessor

    private var isListening = false
    private var areNotificationsEnabled = true
    private var mutedPackages = listOf<String>()

    private lateinit var notificationService: NotificationService
    private lateinit var notificationBridge: NotificationsFlutterBridge
    private lateinit var prefs: KMPPrefs

    override fun onCreate() {
        val injectionComponent = (applicationContext as CobbleApplication).component

        coroutineScope = CoroutineScope(
                SupervisorJob() + injectionComponent.createExceptionHandler()
        )

        connectionLooper = injectionComponent.createConnectionLooper()
        notificationService = injectionComponent.createNotificationService()
        notificationBridge = injectionComponent.createNotificationsFlutterBridge()
        flutterPreferences = injectionComponent.createFlutterPreferences()
        prefs = injectionComponent.createKMPPrefs()
        notificationProcessor = injectionComponent.createNotificationProcessor()

        super.onCreate()
        _isActive.value = true
        Timber.d("NotificationListener created")
    }

    override fun onDestroy() {
        Timber.d("NotificationListener destroyed")
        _isActive.value = false
        super.onDestroy()

        coroutineScope.cancel()
    }

    override fun onListenerConnected() {
        isListening = true
        controlListenerHints()
        observeNotificationToggle()
        observeMutedPackages()
        Timber.d("NotificationListener connected")
    }

    override fun onListenerDisconnected() {
        isListening = false
        Timber.d("NotificationListener disconnected")
    }

    private fun getNotificationGroup(sbn: StatusBarNotification): List<StatusBarNotification> {
        return this.activeNotifications.filter { it.groupKey == sbn.groupKey }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (isListening && areNotificationsEnabled) {
            if (sbn == null) return
            if (sbn.packageName == applicationContext.packageName) return // Don't show a notification if it's us
            if (sbn.notification.extras.containsKey(NotificationCompat.EXTRA_MEDIA_SESSION)) {
                // Do not notify for media notifications
                return
            }
            try {
                val channels = getNotificationChannels(sbn.packageName, sbn.user)
                channels?.forEach {
                    notificationBridge.updateChannel(it.id, sbn.packageName, false, (it.name
                            ?: it.id).toString(), it.description ?: "")
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to get notif channels from ${sbn.packageName}")
            }
            if (NotificationCompat.getLocalOnly(sbn.notification)) return // ignore local notifications TODO: respect user preference
            if (sbn.notification.flags and Notification.FLAG_ONGOING_EVENT != 0) return // ignore ongoing notifications
            if (mutedPackages.contains(sbn.packageName)) return // ignore muted packages
            coroutineScope.launch {
                if (prefs.sensitiveDataLoggingEnabled.firstOrNull() == true) {
                    Timber.d("Notification posted: ${sbn.packageName}")
                    Timber.d("This listener instance is: ${this.hashCode()}")
                    Timber.d("Notification: ${sbn.notification}\n${sbn.notification.extras}")
                }
            }

            if (sbn.groupKey != null) {
                val group = getNotificationGroup(sbn)
                notificationProcessor.processGroupNotification(group.toNotificationGroup())
            } else {
                notificationProcessor.processNotification(sbn)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (isListening) {
            Timber.d("Notification removed: ${sbn.packageName}")

            val notif = notificationBridge.activeNotifs.toList().firstOrNull { it.second.id == sbn.id && it.second.packageName == sbn.packageName }?.first
            if (notif != null) {
                notificationBridge.dismiss(notif)
            }
        }
    }

    override fun onNotificationChannelModified(pkg: String?, user: UserHandle?, channel: NotificationChannel?, modificationType: Int) {
        if (pkg != null && channel != null) {
            val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel.id
            } else {
                "miscellaneous"
            }

            val packageId = pkg
            val delete = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                modificationType == NOTIFICATION_CHANNEL_OR_GROUP_DELETED
            } else {
                false
            }
            val channelDesc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel.description ?: ""
            } else {
                ""
            }
            val channelName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channel.name.toString()
            } else {
                "Miscellaneous"
            }
            notificationBridge.updateChannel(channelId, packageId, delete, channelName, channelDesc)
        }
    }

    private fun controlListenerHints() {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            combine(
                    flutterPreferences.mutePhoneNotificationSounds,
                    flutterPreferences.mutePhoneCallSounds,
                    connectionLooper.connectionState
            ) { mutePhoneNotificationSounds, mutePhoneCallSounds, connectionState ->
                if (connectionState is ConnectionState.Disconnected) {
                    // Do nothing. Listener will be unbound anyway
                    return@combine
                }

                val connected = connectionState is ConnectionState.Connected

                val listenerHints = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    var hints = 0

                    if (connected && mutePhoneNotificationSounds) {
                        hints = hints or HINT_HOST_DISABLE_NOTIFICATION_EFFECTS
                    }

                    if (connected && mutePhoneCallSounds) {
                        hints = hints or HINT_HOST_DISABLE_CALL_EFFECTS
                    }

                    hints
                } else {
                    if (connected && (mutePhoneCallSounds || mutePhoneNotificationSounds)) {
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

    private fun observeMutedPackages() {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            flutterPreferences.mutedNotifPackages.collect {
                Timber.d("${it}")
                mutedPackages = it ?: listOf()
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