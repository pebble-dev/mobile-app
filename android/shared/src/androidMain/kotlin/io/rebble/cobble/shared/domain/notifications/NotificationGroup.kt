package io.rebble.cobble.shared.domain.notifications

import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

data class NotificationGroup(
    val groupKey: String,
    val summary: StatusBarNotification?,
    val children: List<StatusBarNotification>
)

fun List<StatusBarNotification>.toNotificationGroup(): NotificationGroup {
    val mutable = toMutableList()
    val summary = mutable.firstOrNull { NotificationCompat.isGroupSummary(it.notification) }
    summary?.let { mutable.remove(it) }

    val groupKey =
        summary?.groupKey ?: mutable.firstOrNull()?.groupKey
            ?: throw IllegalArgumentException("Notification is not part of a group")

    return NotificationGroup(
        groupKey,
        summary,
        mutable
    )
}