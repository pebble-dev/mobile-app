package io.rebble.cobble.notifications

import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

val StatusBarNotification.shouldDisplayGroupSummary: Boolean
    get() {
        // Check if the group is from a package that should not display group summaries
        return when (packageName) {
            "com.google.android.gm" -> false
            else -> true
        }
    }