package io.rebble.fossil.notifications

import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.flutter.Log

class NotificationListener : NotificationListenerService() {
    private var isListening = false
    private val logTag: String = "FossilNotifService"
    private var onNotif: ((StatusBarNotification) -> Unit)? = null
    private var onNotifRemoved: ((StatusBarNotification) -> Unit)? = null

    override fun onListenerConnected() {
        isListening = true
    }

    override fun onListenerDisconnected() {
        isListening = false
    }

    private var notifStates: MutableMap<String, MutableMap<Int, String>> = mutableMapOf() //TODO: Remove dismissed notifs from me

    private fun sendNotif(pkg: String, sender: String, subject: String, content: String) {
        val intent = Intent("io.rebble.fossil.NOTIFICATION_BROADCAST")
        val bundle = Bundle()
        bundle.putString("pkg", pkg)
        bundle.putString("sender", sender)
        bundle.putString("subject", subject)
        bundle.putString("content", content)
        intent.putExtra("notification", bundle)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
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
}