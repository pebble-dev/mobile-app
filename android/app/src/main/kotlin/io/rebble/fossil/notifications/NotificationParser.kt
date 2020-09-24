package io.rebble.fossil.notifications

import android.app.Notification
import android.content.Context
import android.provider.Telephony
import android.service.notification.StatusBarNotification
import io.rebble.libpebblecommon.blobdb.NotificationSource

fun StatusBarNotification.parseData(context: Context): ParsedNotification {
    val pm = context.packageManager

    val sender = notification.extras[Notification.EXTRA_TITLE] as? String
            ?: pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)) as String

    val subject = notification.extras[Notification.EXTRA_TEXT] as? String
            ?: notification.extras[Notification.EXTRA_BIG_TEXT] as? String ?: ""

    val content = ""

    return ParsedNotification(
            subject, sender, content, packageToSource(context, packageName)
    )
}

private fun packageToSource(context: Context, packageName: String): NotificationSource {
    //TODO: Check for other email clients
    return when (packageName) {
        "com.google.android.gm" -> NotificationSource.Email
        "com.facebook.katana" -> NotificationSource.Facebook
        "com.twitter.android", "com.twitter.android.lite" -> NotificationSource.Twitter
        Telephony.Sms.getDefaultSmsPackage(context) -> NotificationSource.SMS
        else -> NotificationSource.Generic
    }
}