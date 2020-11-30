package io.rebble.cobble.notifications

import io.rebble.libpebblecommon.packets.blobdb.NotificationSource
import io.rebble.libpebblecommon.packets.blobdb.PushNotification


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