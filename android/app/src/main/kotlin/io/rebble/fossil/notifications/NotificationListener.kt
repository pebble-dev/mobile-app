package io.rebble.fossil.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import io.flutter.Log
import io.rebble.fossil.FossilApplication
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

    private var notifStates: MutableMap<String, MutableMap<Int, ParsedNotification>> = mutableMapOf() //TODO: Remove dismissed notifs from me

    private fun sendNotif(parsedNotification: ParsedNotification) {
        GlobalScope.launch {
            notificationService.send(parsedNotification.toBluetoothPacket())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (isListening) {
            if (sbn == null) return
            if (sbn.packageName == applicationContext.packageName) return // Don't show a notification if it's us

            val parsedNotification = sbn.parseData(applicationContext)

            // If the content is the exact same as it was before (and the notif isnt new / previously dismissed), ignore the new notif
            //TODO: This can likely be considerably cleaner
            if (notifStates.containsKey(sbn.packageName) && notifStates[sbn.packageName]!!.containsKey(sbn.id)) {
                if (notifStates[sbn.packageName]!![sbn.id]!! == parsedNotification) {
                    return
                } else {
                    notifStates[sbn.packageName]!!.set(sbn.id, parsedNotification)
                }
            } else {
                if (!notifStates.containsKey(sbn.packageName)) notifStates[sbn.packageName] = mutableMapOf()
                notifStates[sbn.packageName]!![sbn.id] = parsedNotification
            }

            sendNotif(parsedNotification)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (isListening) {
            Log.d(logTag, "Notification removed: ${sbn?.packageName}")
            //TODO: Dismissing on watch
        }
    }
}