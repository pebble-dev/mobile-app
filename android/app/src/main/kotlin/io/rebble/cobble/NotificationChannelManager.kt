package io.rebble.cobble

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import io.rebble.cobble.shared.util.NotificationId
import javax.inject.Inject

class NotificationChannelManager
    @Inject
    constructor(context: Context) {
        init {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService<NotificationManager>()!!

                notificationManager.createNotificationChannel(
                    NotificationChannel(
                        NotificationId.NOTIFICATION_CHANNEL_WATCH_CONNECTED,
                        context.getString(R.string.connected),
                        NotificationManager.IMPORTANCE_LOW
                    )
                )

                notificationManager.createNotificationChannel(
                    NotificationChannel(
                        NotificationId.NOTIFICATION_CHANNEL_WATCH_CONNECTING,
                        context.getString(R.string.connecting),
                        NotificationManager.IMPORTANCE_LOW
                    )
                )

                notificationManager.createNotificationChannel(
                    NotificationChannel(
                        NotificationId.NOTIFICATION_CHANNEL_WARNINGS,
                        context.getString(R.string.warnings),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )

                notificationManager.createNotificationChannel(
                    NotificationChannel(
                        NotificationId.NOTIFICATION_CHANNEL_JOBS,
                        context.getString(R.string.jobs),
                        NotificationManager.IMPORTANCE_MIN
                    )
                )
            }
        }
    }