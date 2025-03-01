package io.rebble.cobble.shared.domain.notifications.calls

import android.app.PendingIntent

enum class CallNotificationType {
    RINGING,
    ONGOING
}

data class CallNotification(
    val packageName: String,
    val answer: PendingIntent?,
    val decline: PendingIntent?,
    val hangUp: PendingIntent?,
    val type: CallNotificationType,
    val contactHandle: String?,
    val contactName: String?
)