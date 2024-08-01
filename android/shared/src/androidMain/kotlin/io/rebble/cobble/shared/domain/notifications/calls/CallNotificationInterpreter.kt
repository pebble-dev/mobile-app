package io.rebble.cobble.shared.domain.notifications.calls

import android.service.notification.StatusBarNotification

interface CallNotificationInterpreter {
    fun processCallNotification(sbn: StatusBarNotification): CallNotification?
}