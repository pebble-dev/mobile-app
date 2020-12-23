package io.rebble.cobble

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import javax.inject.Inject

class NotificationChannelManager @Inject constructor(context: Context) {
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService<NotificationManager>()!!

            notificationManager.createNotificationChannel(
                    NotificationChannel(
                            NOTIFICATION_CHANNEL_WATCH_CONNECTED,
                            context.getString(R.string.connected),
                            NotificationManager.IMPORTANCE_LOW
                    )
            )

            notificationManager.createNotificationChannel(
                    NotificationChannel(
                            NOTIFICATION_CHANNEL_WATCH_CONNECTING,
                            context.getString(R.string.connecting),
                            NotificationManager.IMPORTANCE_LOW
                    )
            )

            notificationManager.createNotificationChannel(
                    NotificationChannel(
                            NOTIFICATION_CHANNEL_WARNINGS,
                            context.getString(R.string.warnings),
                            NotificationManager.IMPORTANCE_DEFAULT
                    )
            )
        }
    }
}

val NOTIFICATION_CHANNEL_WATCH_CONNECTED = "WATCH_CONNECTED"
val NOTIFICATION_CHANNEL_WATCH_CONNECTING = "WATCH_CONNECTING"
val NOTIFICATION_CHANNEL_WARNINGS = "WARNINGS"