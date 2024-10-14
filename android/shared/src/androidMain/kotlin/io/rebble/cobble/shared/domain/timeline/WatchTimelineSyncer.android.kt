package io.rebble.cobble.shared.domain.timeline

import androidx.core.app.NotificationCompat
import io.rebble.cobble.shared.AndroidPlatformContext
import io.rebble.cobble.shared.PlatformContext

actual fun displayWatchFullWarning(context: PlatformContext) {
    require(context is AndroidPlatformContext)

    val notif = NotificationCompat.Builder(context.applicationContext, "WARNINGS")
            .setContentTitle("Your watch is full")
            .setContentText("We could not sync all timeline pins to the watch.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .build()

    val notificationManager = context.applicationContext.getSystemService(android.app.NotificationManager::class.java)
    notificationManager.notify(2, notif)
}