package io.rebble.fossil.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import io.rebble.fossil.FossilApplication
import io.rebble.libpebblecommon.services.notification.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber

class NotificationListener : NotificationListenerService() {
    private lateinit var coroutineScope: CoroutineScope

    private var isListening = false

    private var notifStates: MutableMap<NotificationKey, ParsedNotification> = mutableMapOf()

    private lateinit var notificationService: NotificationService

    override fun onCreate() {
        val injectionComponent = (applicationContext as FossilApplication).component

        coroutineScope = CoroutineScope(
                SupervisorJob() + injectionComponent.createExceptionHandler()
        )

        notificationService = injectionComponent.createNotificationService()

        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()

        coroutineScope.cancel()
    }

    override fun onListenerConnected() {
        isListening = true
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
        if (isListening) {
            if (sbn == null) return
            if (sbn.packageName == applicationContext.packageName) return // Don't show a notification if it's us

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
}