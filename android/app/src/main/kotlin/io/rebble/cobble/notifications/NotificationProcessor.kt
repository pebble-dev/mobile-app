package io.rebble.cobble.notifications

import android.app.Notification
import android.service.notification.StatusBarNotification
import android.text.SpannableString
import androidx.core.app.NotificationCompat
import io.rebble.cobble.bridges.background.NotificationsFlutterBridge
import io.rebble.cobble.data.NotificationAction
import io.rebble.cobble.data.NotificationGroup
import io.rebble.cobble.data.NotificationMessage
import io.rebble.cobble.shared.database.dao.PersistedNotificationDao
import io.rebble.cobble.shared.database.entity.PersistedNotification
import io.rebble.libpebblecommon.packets.blobdb.BlobResponse
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.hours

@Singleton
class NotificationProcessor @Inject constructor(
        exceptionHandler: CoroutineExceptionHandler,
        private val notificationBridge: NotificationsFlutterBridge,
        private val persistedNotifDao: PersistedNotificationDao
) {
    val coroutineScope = CoroutineScope(
            SupervisorJob() + exceptionHandler
    )

    private val activeGroups = mutableMapOf<String, NotificationGroup>()

    private data class DisplayActorArgs(
            val packageId: String,
            val notifId: Long, val tagId: String?, val title: String,
            val text: String, val category: String, val color: Int, val messages: List<NotificationMessage>,
            val actions: List<NotificationAction>,
            val sbn: StatusBarNotification
    )

    private val displayActor = coroutineScope.actor<DisplayActorArgs>(capacity = Channel.UNLIMITED) {
        for (notification in channel) {
            val (packageId, notifId, tagId, title, text, category, color, messages, actions, sbn) = notification

            if (persistedNotifDao.getDuplicates(sbn.key, sbn.packageName, title, text).isNotEmpty()) {
                Timber.d("Ignoring duplicate notification ${sbn.key}")
                continue
            }
            persistedNotifDao.insert(PersistedNotification(
                    sbn.key, sbn.packageName, sbn.postTime, title, text, sbn.groupKey
            ))

            var result = withContext(Dispatchers.Main) {
                notificationBridge.handleNotification(
                        packageId, notifId, tagId, title, text, category, color, messages, actions
                )
            } ?: continue

            while (result.second == BlobResponse.BlobStatus.TryLater) {
                Timber.w("BlobDB is busy, retrying in 1s")
                delay(1000)
                result = withContext(Dispatchers.Main) {
                    notificationBridge.handleNotification(
                            packageId, notifId, tagId, title, text, category, color, messages, actions
                    )
                } ?: continue
            }
            Timber.d(result.second.toString())
            persistedNotifDao.deleteOlderThan(System.currentTimeMillis() - 1.hours.inWholeMilliseconds)
            withContext(Dispatchers.Main) {
                notificationBridge.activeNotifs[result.first.itemId.get()] = sbn
            }
            delay(10)
        }
    }

    fun processGroupNotification(notificationGroup: NotificationGroup) {
        val summary = notificationGroup.summary
        if (summary == null) {
            getNewGroupItems(notificationGroup).forEach(::processNotification)
        } else {
            val newItems = getNewGroupItems(notificationGroup).asReversed() // Reverse so latest notification is top
            if (!newItems.any { !NotificationCompat.isGroupSummary(it.notification) && it.notification.equals(summary) }) {
                newItems.forEach(::processNotification)
            } else {
                if (summary.shouldDisplayGroupSummary) {
                    processNotification(summary)
                }
                newItems.forEach(::processNotification)
            }
        }
        activeGroups[notificationGroup.groupKey] = notificationGroup
    }

    private fun getNewGroupItems(notificationGroup: NotificationGroup): List<StatusBarNotification> {
        val newItems = notificationGroup.children.filter { notif ->
            // Notification is functionally equal to an active notification
            notificationBridge.activeNotifs.values.find {
                it.groupKey == notificationGroup.groupKey &&
                        it.packageName == notif.packageName &&
                        it.id == notif.id &&
                        NotificationCompat.getShowWhen(it.notification) == NotificationCompat.getShowWhen(notif.notification) &&
                        it.notification.extras.keySet() == notif.notification.extras.keySet()
            } == null
        }
        return newItems
    }

    fun processNotification(sbn: StatusBarNotification) {
        Timber.v("Processing notification ${sbn.key}")
        val notification = sbn.notification
        val tagId = notification.channelId
        val title = notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                ?: notification.extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
                ?: ""
        val text = extractBody(notification)

        val messages: List<NotificationMessage>? = extractMessages(notification)

        val actions = notification.actions?.map {
            NotificationAction(it.title.toString(), !it.remoteInputs.isNullOrEmpty())
        } ?: listOf()

        displayActor.trySend(DisplayActorArgs(
                sbn.packageName,
                sbn.id.toLong(),
                tagId,
                title,
                text,
                notification.category ?: "",
                notification.color,
                messages ?: listOf(),
                actions,
                sbn
        ))
    }

    private fun extractBody(notification: Notification): String {
        var text = notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
                ?: notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
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
            val infoText = notification.extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString()
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