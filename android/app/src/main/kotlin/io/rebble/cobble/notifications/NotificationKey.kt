package io.rebble.cobble.notifications

import android.service.notification.StatusBarNotification

data class NotificationKey(val pkg: String, val id: Int, val tag: String?) {
    constructor(sbn: StatusBarNotification) : this(sbn.packageName, sbn.id, sbn.tag)
}