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
import com.benasher44.uuid.Uuid
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.data.NotificationMessage
import io.rebble.cobble.data.toNotificationGroup
import io.rebble.cobble.datasources.FlutterPreferences
import io.rebble.cobble.shared.database.dao.NotificationChannelDao
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
    private lateinit var activeNotifsState: MutableStateFlow<Map<Uuid, StatusBarNotification>>
    private lateinit var notificationChannelDao: NotificationChannelDao

    private var isListening = false
    private var areNotificationsEnabled = true
    private var mutedPackages = listOf<String>()

    private lateinit var notificationService: NotificationService
    private lateinit var prefs: KMPPrefs

    override fun onCreate() {
        val injectionComponent = (applicationContext as CobbleApplication).component

        coroutineScope = CoroutineScope(
                SupervisorJob() + injectionComponent.createExceptionHandler()
        )

        connectionLooper = injectionComponent.createConnectionLooper()
        notificationService = injectionComponent.createNotificationService()
        flutterPreferences = injectionComponent.createFlutterPreferences()
        prefs = injectionComponent.createKMPPrefs()
        notificationProcessor = injectionComponent.createNotificationProcessor()
        activeNotifsState = injectionComponent.createActiveNotifsState()
        notificationChannelDao = injectionComponent.createNotificationChannelDao()

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
            coroutineScope.launch {
                val sensitiveLogging = prefs.sensitiveDataLoggingEnabled.firstOrNull() == true
                try {
                    val channels = getNotificationChannels(sbn.packageName, sbn.user)
                    /*if (sensitiveLogging) {
                        channels.forEach {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                Timber.v("Channel: ${it.id}, ${it.name}, ${it.description} ${it.conversationId}")
                            } else {
                                Timber.v("Channel: ${it.id}, ${it.name}, ${it.description} (no conversationId as old android)")
                            }
                        }
                    }*/
                    notificationChannelDao.insertAllIfNotExists(
                            channels.map {
                                io.rebble.cobble.shared.database.entity.NotificationChannel(
                                        sbn.packageName,
                                        it.id,
                                        it.name.toString(),
                                        it.description,
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                            it.conversationId
                                        } else {
                                            null
                                        },
                                        true
                                )
                            }
                    )
                } catch (e: Exception) {
                    Timber.w(e, "Failed to get notif channels from ${sbn.packageName}")
                }
                if (NotificationCompat.getLocalOnly(sbn.notification)) return@launch // ignore local notifications TODO: respect user preference
                if (sbn.notification.flags and Notification.FLAG_ONGOING_EVENT != 0) return@launch // ignore ongoing notifications
                if (mutedPackages.contains(sbn.packageName)) return@launch // ignore muted packages
                if (sensitiveLogging) {
                    Timber.d("Notification posted: ${sbn.packageName}")
                    Timber.d("This listener instance is: ${this.hashCode()}")
                    Timber.d("Notification: ${sbn.notification}\n${sbn.notification.extras}")
                }

                if (sbn.groupKey != null) {
                    try {
                        val group = getNotificationGroup(sbn).toNotificationGroup()
                        notificationProcessor.processGroupNotification(group)
                    } catch (_: IllegalArgumentException) {
                        notificationProcessor.processNotification(sbn)
                    }
                } else {
                    notificationProcessor.processNotification(sbn)
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (isListening) {
            Timber.d("Notification removed: ${sbn.packageName}")

            //TODO: Dismiss notification in kmp
            val notif = activeNotifications
            /*
            val notif = notificationBridge.activeNotifs.toList().firstOrNull { it.second.id == sbn.id && it.second.packageName == sbn.packageName }?.first
            if (notif != null) {
                notificationBridge.dismiss(notif)
            }*/
        }
    }

    override fun onNotificationChannelModified(pkg: String?, user: UserHandle?, channel: NotificationChannel?, modificationType: Int) {
        if (pkg != null && channel != null) {
            coroutineScope.launch {
                val channelId = channel.id
                val packageId = pkg
                val delete = modificationType == NOTIFICATION_CHANNEL_OR_GROUP_DELETED
                val channelDesc = channel.description ?: ""
                val channelName = channel.name.toString()
                val conversation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    channel.conversationId
                } else {
                    null
                }

                val existing = notificationChannelDao.get(packageId, channelId)
                if (existing != null) {
                    if (delete) {
                        notificationChannelDao.delete(existing)
                    } else {
                        notificationChannelDao.update(existing.copy(
                                name = channelName,
                                description = channelDesc,
                                conversationId = conversation
                        ))
                    }
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
            prefs.mutedPackages.collect {
                mutedPackages = it.toList()
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