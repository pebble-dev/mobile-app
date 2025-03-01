package io.rebble.cobble.shared.domain.notifications.calls

import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

class DiscordCallNotificationInterpreter : CallNotificationInterpreter {
    override fun processCallNotification(sbn: StatusBarNotification): CallNotification? {
        val joinCallAction =
            sbn.notification.actions.firstOrNull {
                it.title.toString().contains("Join Call")
            }
        val declineAction =
            sbn.notification.actions.firstOrNull {
                it.title.toString().contains(
                    "Decline"
                )
            }

        if (joinCallAction != null && declineAction != null) {
            val contactName =
                NotificationCompat.getContentText(
                    sbn.notification
                )?.trim()?.split(" ")?.firstOrNull()?.let {
                    "Discord\n$it"
                }
            return CallNotification(
                sbn.packageName,
                joinCallAction.actionIntent,
                declineAction.actionIntent,
                null,
                CallNotificationType.RINGING,
                "Discord",
                contactName ?: "Discord Call"
            )
        } else {
            return null
        }
    }
}