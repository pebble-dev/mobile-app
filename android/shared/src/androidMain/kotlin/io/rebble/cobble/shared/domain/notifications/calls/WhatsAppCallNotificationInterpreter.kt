package io.rebble.cobble.shared.domain.notifications.calls

import android.service.notification.StatusBarNotification

class WhatsAppCallNotificationInterpreter: CallNotificationInterpreter {
    override fun processCallNotification(sbn: StatusBarNotification): CallNotification? {
        val acceptAction = sbn.notification.actions.firstOrNull { it.title.toString().contains("Answer", true) }
        val declineAction = sbn.notification.actions.firstOrNull { it.title.toString().contains("Decline", true) }
        val hangUpAction = sbn.notification.actions.firstOrNull { it.title.toString().contains("Hang up", true) }

        if (acceptAction != null && declineAction != null) {
            val contactName = sbn.notification.extras.getCharSequence("android.title")?.toString()?.let {
                "WhatsApp\n$it"
            }
            return CallNotification(
                    sbn.packageName,
                    acceptAction.actionIntent,
                    declineAction.actionIntent,
                    null,
                    CallNotificationType.RINGING,
                    "WhatsApp",
                    contactName ?: "WhatsApp Call"
            )
        } else if (hangUpAction != null) {
            val contactName = sbn.notification.extras.getCharSequence("android.title")?.toString()?.let {
                "WhatsApp\n$it"
            }
            return CallNotification(
                    sbn.packageName,
                    null,
                    null,
                    hangUpAction.actionIntent,
                    CallNotificationType.ONGOING,
                    "WhatsApp",
                    contactName ?: "WhatsApp Call"
            )
        } else {
            return null
        }
    }
}