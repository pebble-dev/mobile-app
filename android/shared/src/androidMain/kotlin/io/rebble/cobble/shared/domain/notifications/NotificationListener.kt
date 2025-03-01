package io.rebble.cobble.shared.domain.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.companion.CompanionDeviceManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.UserHandle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.database.dao.NotificationChannelDao
import io.rebble.cobble.shared.datastore.FlutterPreferences
import io.rebble.cobble.shared.datastore.KMPPrefs
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.errors.GlobalExceptionHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import timber.log.Timber

class NotificationListener : NotificationListenerService(), KoinComponent {
    private val globalExceptionHandler: GlobalExceptionHandler by inject()
    private val coroutineScope: CoroutineScope =
        CoroutineScope(
            SupervisorJob() + globalExceptionHandler + CoroutineName("NotificationListener")
        )
    private val notificationProcessor: NotificationProcessor by inject()
    private val callNotificationProcessor: CallNotificationProcessor by inject()
    private val activeNotifsState: MutableStateFlow<Map<Uuid, StatusBarNotification>> by inject(
        named("activeNotifsState")
    )
    private val notificationChannelDao: NotificationChannelDao by inject()

    // TODO: switch to main prefs once we switch notif pages to flutter
    private val flutterPreferences: FlutterPreferences by inject()
    private val prefs: KMPPrefs by inject()
    private lateinit var companionDeviceManager: CompanionDeviceManager

    private var isListening = false
    private var areNotificationsEnabled = true
    private var mutedPackages = listOf<String>()

    override fun onCreate() {
        super.onCreate()
        companionDeviceManager = getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
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
                    if (companionDeviceManager.associations.isEmpty()) {
                        Logging.w(
                            "No companion devices, listener service has reduced permissions, skipping channel fetch"
                        )
                    } else {
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
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Failed to get notif channels from ${sbn.packageName}")
                }

                if (NotificationCompat.getCategory(sbn.notification) == Notification.CATEGORY_CALL) {
                    callNotificationProcessor.processCallNotification(sbn)
                    return@launch
                }

                if (NotificationCompat.getLocalOnly(
                        sbn.notification
                    )
                ) {
                    return@launch // ignore local notifications TODO: respect user preference
                }
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
            if (NotificationCompat.getCategory(sbn.notification) == Notification.CATEGORY_CALL) {
                callNotificationProcessor.processCallNotificationDismissal(sbn)
            } else {
                coroutineScope.launch {
                    notificationProcessor.processDismissed(sbn)
                }
            }
        }
    }

    override fun onNotificationChannelModified(
        pkg: String?,
        user: UserHandle?,
        channel: NotificationChannel?,
        modificationType: Int
    ) {
        if (pkg != null && channel != null) {
            coroutineScope.launch {
                val channelId = channel.id
                val packageId = pkg
                val delete = modificationType == NOTIFICATION_CHANNEL_OR_GROUP_DELETED
                val channelDesc = channel.description ?: ""
                val channelName = channel.name.toString()
                val conversation =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        channel.conversationId
                    } else {
                        null
                    }

                val existing = notificationChannelDao.get(packageId, channelId)
                if (existing != null) {
                    if (delete) {
                        notificationChannelDao.delete(existing)
                    } else {
                        notificationChannelDao.update(
                            existing.copy(
                                name = channelName,
                                description = channelDesc,
                                conversationId = conversation
                            )
                        )
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
                ConnectionStateManager.connectionState
            ) { mutePhoneNotificationSounds, mutePhoneCallSounds, connectionState ->
                if (connectionState is ConnectionState.Disconnected) {
                    // Do nothing. Listener will be unbound anyway
                    return@combine
                }

                val connected = connectionState is ConnectionState.Connected

                var listenerHints = 0
                if (connected && mutePhoneNotificationSounds) {
                    listenerHints = listenerHints or HINT_HOST_DISABLE_NOTIFICATION_EFFECTS
                }

                if (connected && mutePhoneCallSounds) {
                    listenerHints = listenerHints or HINT_HOST_DISABLE_CALL_EFFECTS
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
        val isActive: StateFlow<Boolean> by Companion::_isActive

        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, NotificationListener::class.java)
        }
    }
}