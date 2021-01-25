package io.rebble.cobble.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.dexterous.flutterlocalnotifications.models.Time
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.bridges.background.NotificationsFlutterBridge
import io.rebble.cobble.data.NotificationAction
import io.rebble.cobble.data.NotificationMessage
import io.rebble.cobble.data.TimelineAttribute
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.libpebblecommon.PacketPriority
import io.rebble.libpebblecommon.packets.blobdb.*
import io.rebble.libpebblecommon.services.notification.NotificationService
import io.rebble.libpebblecommon.structmapper.SUUID
import io.rebble.libpebblecommon.structmapper.StructMapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import kotlin.random.Random

class NotificationListener : NotificationListenerService() {
    private lateinit var coroutineScope: CoroutineScope

    private var isListening = false

    private lateinit var notificationService: NotificationService
    private lateinit var notificationBridge: NotificationsFlutterBridge

    override fun onCreate() {
        val injectionComponent = (applicationContext as CobbleApplication).component

        coroutineScope = CoroutineScope(
                SupervisorJob() + injectionComponent.createExceptionHandler()
        )

        notificationService = injectionComponent.createNotificationService()
        notificationBridge = injectionComponent.createNotificationsFlutterBridge()

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
    }

    override fun onListenerDisconnected() {
        isListening = false
    }

    @ExperimentalStdlibApi
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (isListening) {
            if (sbn == null) return
            if (sbn.packageName == applicationContext.packageName) return // Don't show a notification if it's us
            if (sbn.notification.extras.containsKey(NotificationCompat.EXTRA_MEDIA_SESSION)) {
                // Do not notify for media notifications
                return
            }
            if (NotificationCompat.getLocalOnly(sbn.notification)) return // ignore local notifications TODO: respect user preference
            if (sbn.notification.flags and Notification.FLAG_ONGOING_EVENT != 0) return // ignore ongoing notifications
            if (sbn.notification.group != null && !NotificationCompat.isGroupSummary(sbn.notification)) return

            var tagId: String? = null
            var tagName: String? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tagId = sbn.notification.channelId
                try {
                    tagName = (applicationContext.createPackageContext(sbn.packageName, 0).getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).getNotificationChannel(sbn.notification.channelId).name.toString()
                } catch (e: SecurityException) {}
                if (tagName == null) tagName = tagId
            }
            val title = sbn.notification.extras[Notification.EXTRA_TITLE] as? String
                    ?: applicationContext.packageManager.getApplicationLabel(applicationContext.packageManager.getApplicationInfo(packageName, 0)) as String

            val text = sbn.notification.extras[Notification.EXTRA_TEXT] as? String
                    ?: sbn.notification.extras[Notification.EXTRA_BIG_TEXT] as? String ?: ""

            val actions = sbn.notification.actions.map {
                NotificationAction(it.title as String, !it.remoteInputs.isNullOrEmpty())
            }

            var messages: List<NotificationMessage>? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                messages = sbn.notification.extras.getParcelableArray(Notification.EXTRA_MESSAGES)?.map {
                    NotificationMessage(((it as Bundle)["sender"] as String), it["text"] as String, it["time"] as Long)
                }
            }
            coroutineScope.launch(Dispatchers.Main.immediate) {
                var result = notificationBridge.handleNotification(sbn.packageName, sbn.id.toLong(), tagId, tagName, title, text, messages?: listOf(), actions)
                while (result.second == BlobResponse.BlobStatus.TryLater) {
                    delay(1000)
                    result = notificationBridge.handleNotification(sbn.packageName, sbn.id.toLong(), tagId, tagName, title, text, messages?: listOf(), actions)
                }
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

    companion object {
        private val _isActive = MutableStateFlow(false)
        val isActive: StateFlow<Boolean> by ::_isActive
    }
}