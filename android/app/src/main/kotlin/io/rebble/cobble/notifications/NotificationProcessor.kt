package io.rebble.cobble.notifications

import android.app.Notification
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import io.rebble.cobble.bridges.background.NotificationsFlutterBridge
import io.rebble.cobble.data.NotificationAction
import io.rebble.cobble.data.NotificationMessage
import io.rebble.libpebblecommon.packets.blobdb.BlobResponse
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationProcessor @Inject constructor(
        exceptionHandler: CoroutineExceptionHandler,
        private val notificationBridge: NotificationsFlutterBridge
) {
    val coroutineScope = CoroutineScope(
            SupervisorJob() + exceptionHandler
    )

    fun processNotification(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val tagId = notification.channelId
        val title = notification.extras.getString(Notification.EXTRA_TITLE)
                ?: notification.extras.getString(Notification.EXTRA_CONVERSATION_TITLE)
                ?: ""
        val text = extractBody(notification)
        val messages: List<NotificationMessage>? = extractMessages(notification)

        val actions = notification.actions?.map {
            NotificationAction(it.title.toString(), !it.remoteInputs.isNullOrEmpty())
        } ?: listOf()

        coroutineScope.launch(Dispatchers.Main) {
            var result: Pair<TimelineItem, BlobResponse.BlobStatus>? = notificationBridge.handleNotification(sbn.packageName, sbn.id.toLong(), tagId, title, text, notification.category
                    ?: "", notification.color, messages ?: listOf(), actions)
                    ?: return@launch

            while (result!!.second == BlobResponse.BlobStatus.TryLater) {
                delay(1000)
                result = notificationBridge.handleNotification(sbn.packageName, sbn.id.toLong(), tagId, title, text, notification.category
                        ?: "", notification.color, messages ?: listOf(), actions)
                        ?: return@launch
            }
            Timber.d(result.second.toString())
            notificationBridge.activeNotifs[result.first.itemId.get()] = sbn
        }
    }

    private fun extractBody(notification: Notification): String {
        var text = notification.extras.getString(Notification.EXTRA_TEXT)
                ?: notification.extras.getString(Notification.EXTRA_BIG_TEXT)
                ?: ""

        // If the text is empty, try to get it from the text lines
        if (text.isBlank()) {
            val textLines = notification.extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            textLines?.let {
                text = textLines.joinToString("\n")
            }
        }
        // If the text is still empty, try to get it from the info text
        if (text.isBlank()) {
            val infoText = notification.extras.getString(Notification.EXTRA_INFO_TEXT)
            infoText?.let {
                text = it
            }
        }
        // All else fails, try to get it from the ticker text
        if (text.isBlank()) {
            val tickerText = notification.tickerText
            tickerText?.let {
                text = it.toString()
            }
        }

        text = text.replace('\u2009', ' ') // Replace thin space with normal space (watch doesn't support it)
        return text
    }

    private fun extractMessages(notification: Notification): List<NotificationMessage>? {
        val messagesArr = notification.extras.getParcelableArray(Notification.EXTRA_MESSAGES)
        return messagesArr?.let {
            NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
                    ?.messages?.map {
                        NotificationMessage(it.person?.name.toString(), it.text.toString(), it.timestamp)
                    }
        }
    }
}