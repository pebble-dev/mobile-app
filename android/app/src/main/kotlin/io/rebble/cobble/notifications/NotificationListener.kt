package io.rebble.cobble.notifications

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.UserHandle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import io.rebble.cobble.bridges.background.NotificationsFlutterBridge
import io.rebble.cobble.data.NotificationAction
import io.rebble.cobble.data.NotificationMessage
import io.rebble.cobble.datasources.FlutterPreferences
import io.rebble.libpebblecommon.packets.blobdb.BlobResponse
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
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
    private var mutedPackages = listOf<String>()

    private lateinit var notificationService: NotificationService
    private lateinit var notificationBridge: NotificationsFlutterBridge

    override fun onCreate() {
        val injectionComponent = (applicationContext as CobbleApplication).component

        coroutineScope = CoroutineScope(
                SupervisorJob() + injectionComponent.createExceptionHandler()
        )

        connectionLooper = injectionComponent.createConnectionLooper()
        notificationService = injectionComponent.createNotificationService()
        notificationBridge = injectionComponent.createNotificationsFlutterBridge()
        flutterPreferences = injectionComponent.createFlutterPreferences()

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            unbindOnWatchDisconnect()
        }

        controlListenerHints()
        observeNotificationToggle()
        observeMutedPackages()
        Timber.d("NotificationListener connected")
    }

    override fun onListenerDisconnected() {
        isListening = false
        Timber.d("NotificationListener disconnected")
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val channels = getNotificationChannels(sbn.packageName, sbn.user)
                    channels?.forEach {
                        notificationBridge.updateChannel(it.id, sbn.packageName, false, (it.name
                                ?: it.id).toString(), it.description ?: "")
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Failed to get notif channels from ${sbn.packageName}")
                }
            }
            if (NotificationCompat.getLocalOnly(sbn.notification)) return // ignore local notifications TODO: respect user preference
            if (sbn.notification.flags and Notification.FLAG_ONGOING_EVENT != 0) return // ignore ongoing notifications
            //if (sbn.notification.group != null && !NotificationCompat.isGroupSummary(sbn.notification)) return
            if (mutedPackages.contains(sbn.packageName)) return // ignore muted packages

            var tagId: String? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tagId = sbn.notification.channelId
            }
            val title = sbn.notification.extras[Notification.EXTRA_TITLE] as? String
                    ?: sbn.notification.extras[Notification.EXTRA_CONVERSATION_TITLE] as? String
                    ?: ""

            val text = sbn.notification.extras[Notification.EXTRA_TEXT] as? String
                    ?: sbn.notification.extras[Notification.EXTRA_BIG_TEXT] as? String ?: ""

            val actions = sbn.notification.actions?.map {
                NotificationAction(it.title.toString(), !it.remoteInputs.isNullOrEmpty())
            } ?: listOf()

            var messages: List<NotificationMessage>? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val messagesArr = sbn.notification.extras.getParcelableArray(Notification.EXTRA_MESSAGES)
                if (messagesArr != null) {
                    val msgstyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(sbn.notification)
                    messages = msgstyle?.messages?.map {
                        NotificationMessage(it.person?.name.toString(), it.text.toString(), it.timestamp)
                    }
                }
            }

            GlobalScope.launch(Dispatchers.Main.immediate) {
                var result: Pair<TimelineItem, BlobResponse.BlobStatus>? = notificationBridge.handleNotification(sbn.packageName, sbn.id.toLong(), tagId, title, text, sbn.notification.category
                        ?: "", sbn.notification.color, messages ?: listOf(), actions)
                        ?: return@launch

                while (result!!.second == BlobResponse.BlobStatus.TryLater) {
                    delay(1000)
                    result = notificationBridge.handleNotification(sbn.packageName, sbn.id.toLong(), tagId, title, text, sbn.notification.category
                            ?: "", sbn.notification.color, messages ?: listOf(), actions)
                            ?: return@launch
                }
                Timber.d(result.second.toString())
                notificationBridge.activeNotifs[result.first.itemId.get()] = sbn
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

    @TargetApi(Build.VERSION_CODES.N)
    private fun unbindOnWatchDisconnect() {
        // It is a waste of resources to keep running notification listener in the background when
        // watch disconnects.

        // When watch disconnects, we call requestUnbind() to kill ourselves it and wait for
        // ServiceLifecycleControl to starts up back up when watch reconnects.

        coroutineScope.launch(Dispatchers.Main.immediate) {
            connectionLooper.connectionState.collect {
                if (it is ConnectionState.Disconnected || it is ConnectionState.RecoveryMode) {
                    requestUnbind()
                }
            }
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