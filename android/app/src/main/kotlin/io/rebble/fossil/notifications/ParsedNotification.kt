package io.rebble.fossil.notifications

import io.rebble.libpebblecommon.blobdb.NotificationSource
import io.rebble.libpebblecommon.blobdb.PushNotification

data class ParsedNotification(
        val subject: String,
        val sender: String,
        val message: String,
        val source: NotificationSource
) {
    fun toBluetoothPacket(): PushNotification {
        return PushNotification(
                subject = subject,
                sender = sender,
                message = message,
                source = source
        )
    }
}