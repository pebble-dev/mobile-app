package io.rebble.fossil.notifications

import android.app.Notification
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import io.flutter.Log
import io.rebble.fossil.FossilApplication
import io.rebble.libpebblecommon.blobdb.NotificationSource
import io.rebble.libpebblecommon.blobdb.PushNotification
import io.rebble.libpebblecommon.services.notification.NotificationService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotificationListener : NotificationListenerService() {
    private var isListening = false
    private val logTag: String = "FossilNotifService"

    private lateinit var notificationService: NotificationService

    override fun onCreate() {
        val injectionComponent = (applicationContext as FossilApplication).component

        notificationService = injectionComponent.createNotificationService()

        super.onCreate()
    }

    override fun onListenerConnected() {
        isListening = true
    }

    override fun onListenerDisconnected() {
        isListening = false
    }

    private var notifStates: MutableMap<String, MutableMap<Int, String>> = mutableMapOf() //TODO: Remove dismissed notifs from me

    private fun sendNotif(pkg: String, sender: String, subject: String, content: String) {
        GlobalScope.launch {
            notificationService.send(PushNotification(subject, sender, content, packageToSource(pkg)))
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (isListening) {
            if (sbn == null) return
            if (sbn.packageName == applicationContext.packageName) return // Don't show a notification if it's us

            val pm = getApplicationContext().getPackageManager()!!
            val sender = sbn.notification.extras[Notification.EXTRA_TITLE] as? String
                    ?: pm.getApplicationLabel(pm.getApplicationInfo(sbn.packageName, 0)) as String
            Log.d(logTag, sbn.notification.extras.toString())
            val subject = sbn.notification.extras[Notification.EXTRA_TEXT] as? String
                    ?: sbn.notification.extras[Notification.EXTRA_BIG_TEXT] as? String ?: ""
            val content = ""

            // If the content is the exact same as it was before (and the notif isnt new / previously dismissed), ignore the new notif
            //TODO: This can likely be considerably cleaner
            if (notifStates.containsKey(sbn.packageName) && notifStates[sbn.packageName]!!.containsKey(sbn.id)) {
                if (notifStates[sbn.packageName]!![sbn.id]!! == sender + subject + content) {
                    return
                } else {
                    notifStates[sbn.packageName]!!.set(sbn.id, sender + subject + content)
                }
            } else {
                if (!notifStates.containsKey(sbn.packageName)) notifStates[sbn.packageName] = mutableMapOf()
                notifStates[sbn.packageName]!![sbn.id] = sender + subject + content
            }
            Log.d(logTag, notifStates[sbn.packageName]!![sbn.id]!!)

            Log.d(logTag, "Notification: ${sbn.packageName}")
            sendNotif(sbn.packageName, sender, subject, content)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (isListening) {
            Log.d(logTag, "Notification removed: ${sbn?.packageName}")
            //TODO: Dismissing on watch
        }
    }

    private fun packageToSource(pkg: String?): NotificationSource {
        //TODO: Check for other email clients
        return when (pkg) {
            "com.google.android.gm" -> NotificationSource.Email
            "com.facebook.katana" -> NotificationSource.Facebook
            "com.twitter.android", "com.twitter.android.lite" -> NotificationSource.Twitter
            Telephony.Sms.getDefaultSmsPackage(this) -> NotificationSource.SMS
            else -> NotificationSource.Generic
        }
    }
}