package io.rebble.cobble.notifications

import android.app.Notification
import android.content.Context
import android.graphics.Color
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import io.rebble.cobble.data.NotificationGroup
import io.rebble.cobble.data.NotificationMessage
import io.rebble.cobble.shared.database.dao.NotificationChannelDao
import io.rebble.cobble.shared.database.dao.PersistedNotificationDao
import io.rebble.cobble.shared.database.entity.NotificationChannel
import io.rebble.cobble.shared.database.entity.PersistedNotification
import io.rebble.cobble.shared.datastore.DEFAULT_MUTED_PACKAGES_VERSION
import io.rebble.cobble.shared.datastore.KMPPrefs
import io.rebble.cobble.shared.datastore.defaultMutedPackages
import io.rebble.cobble.shared.domain.common.SystemAppIDs.notificationsWatchappId
import io.rebble.cobble.shared.domain.notifications.MetaNotificationAction
import io.rebble.libpebblecommon.packets.blobdb.BlobCommand
import io.rebble.libpebblecommon.packets.blobdb.BlobResponse
import io.rebble.libpebblecommon.packets.blobdb.TimelineIcon
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import io.rebble.libpebblecommon.structmapper.SUUID
import io.rebble.libpebblecommon.structmapper.StructMapper
import io.rebble.libpebblecommon.util.PebbleColor
import io.rebble.libpebblecommon.util.TimelineAttributeFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class NotificationProcessor @Inject constructor(
        exceptionHandler: CoroutineExceptionHandler,
        private val persistedNotifDao: PersistedNotificationDao,
        private val blobDBService: BlobDBService,
        private val notificationChannelDao: NotificationChannelDao,
        private val context: Context,
        private val activeNotifsState: MutableStateFlow<Map<UUID, StatusBarNotification>>,
        private val prefs: KMPPrefs,
) {
    val coroutineScope = CoroutineScope(
            SupervisorJob() + exceptionHandler
    )

    private val activeGroups = mutableMapOf<String, NotificationGroup>()

    private data class DisplayActorArgs(
            val packageId: String,
            val notifId: Long, val tagId: String?, val title: String,
            val text: String, val category: String, val color: Int, val messages: List<NotificationMessage>,
            val actions: List<Notification.Action>,
            val sbn: StatusBarNotification
    )

    private fun determineIcon(packageId: String, category: String) = when(packageId) {
        "com.google.android.gm.lite", "com.google.android.gm" -> TimelineIcon.NotificationGmail
        "com.microsoft.office.outlook" -> TimelineIcon.NotificationOutlook
        "com.Slack" -> TimelineIcon.NotificationSlack
        "com.snapchat.android" -> TimelineIcon.NotificationSnapchat
        "com.twitter.android", "com.twitter.android.lite" -> TimelineIcon.NotificationTwitter
        "org.telegram.messenger" -> TimelineIcon.NotificationTelegram
        "com.facebook.katana", "com.facebook.lite" -> TimelineIcon.NotificationFacebook
        "com.facebook.orca" -> TimelineIcon.NotificationFacebookMessenger
        "com.whatsapp" -> TimelineIcon.NotificationWhatsapp

        else -> when (category) {
            Notification.CATEGORY_EMAIL -> TimelineIcon.GenericEmail
            Notification.CATEGORY_MESSAGE -> TimelineIcon.GenericSms
            Notification.CATEGORY_EVENT -> TimelineIcon.TimelineCalendar
            Notification.CATEGORY_PROMO -> TimelineIcon.PayBill
            Notification.CATEGORY_ALARM -> TimelineIcon.AlarmClock
            Notification.CATEGORY_ERROR -> TimelineIcon.GenericWarning
            Notification.CATEGORY_TRANSPORT -> TimelineIcon.AudioCassette
            Notification.CATEGORY_SYSTEM -> TimelineIcon.Settings
            Notification.CATEGORY_REMINDER -> TimelineIcon.NotificationReminder
            Notification.CATEGORY_WORKOUT -> TimelineIcon.Activity
            Notification.CATEGORY_MISSED_CALL -> TimelineIcon.TimelineMissedCall
            Notification.CATEGORY_CALL -> TimelineIcon.IncomingPhoneCall
            Notification.CATEGORY_NAVIGATION, Notification.CATEGORY_LOCATION_SHARING -> TimelineIcon.Location
            Notification.CATEGORY_SOCIAL, Notification.CATEGORY_RECOMMENDATION -> TimelineIcon.NewsEvent
            else -> TimelineIcon.NotificationGeneric
        }
    }

    private suspend fun shouldNotify(packageId: String, channelId: String): Boolean {
        val mutedPackages = prefs.mutedPackages.first()
        if (mutedPackages.contains(packageId)) {
            return false
        }

        val channel = notificationChannelDao.get(packageId, channelId)
        return channel?.shouldNotify ?: true
    }

    private val displayActor = coroutineScope.actor<DisplayActorArgs>(capacity = Channel.UNLIMITED) {
        for (notification in channel) {
            val (packageId, notifId, tagId, title, text, category, color, messages, actions, sbn) = notification

            if (persistedNotifDao.getDuplicates(sbn.key, sbn.packageName, title, text).isNotEmpty()) {
                Timber.d("Ignoring duplicate notification ${sbn.key}")
                continue
            }
            val resolvedPackage = sbn.queryPackage(context)
            if (resolvedPackage == null) {
                Timber.d("Ignoring system/unknown notification ${sbn.key}")
                continue
            }
            persistedNotifDao.insert(PersistedNotification(
                    sbn.key, sbn.packageName, sbn.postTime, title, text, sbn.groupKey
            ))

            if (prefs.defaultMutedPackagesVersion.first() != DEFAULT_MUTED_PACKAGES_VERSION) {
                val current = prefs.mutedPackages.first()
                prefs.setMutedPackages(current + defaultMutedPackages)
            }

            notificationChannelDao.insert(
                    NotificationChannel(
                            sbn.packageName,
                            sbn.notification.channelId,
                            notificationChannelDao.get(sbn.packageName, sbn.notification.channelId)?.name,
                            notificationChannelDao.get(sbn.packageName, sbn.notification.channelId)?.description,
                            notificationChannelDao.get(sbn.packageName, sbn.notification.channelId)?.shouldNotify ?: true
                    )
            )

            if (!shouldNotify(sbn.packageName, sbn.notification.channelId)) {
                Timber.v("Ignoring notification from muted channel/package ${sbn.key}")
                continue
            }

            val itemId = UUID.randomUUID()
            val attributes = mutableListOf(
                    TimelineAttributeFactory.tinyIcon(determineIcon(packageId, category)),
                    TimelineAttributeFactory.title(title.trim()),
                    if (messages.isNotEmpty()) {
                        TimelineAttributeFactory.body(messages.last().text.trim())
                    } else {
                        TimelineAttributeFactory.body(text.trim())
                    }
            )

            if (color > 1) {
                attributes.add(TimelineAttributeFactory.primaryColor(PebbleColor(
                        Color.alpha(color).toUByte(),
                        Color.red(color).toUByte(),
                        Color.green(color).toUByte(),
                        Color.blue(color).toUByte()
                )))
            }

            val pebbleActions = buildList {
                add(
                        TimelineItem.Action(
                            MetaNotificationAction.Dismiss.ordinal.toUByte(),
                            TimelineItem.Action.Type.Dismiss,
                            listOf(TimelineAttributeFactory.title("Dismiss"))
                        )
                )
                actions.forEachIndexed { index, action ->
                    val isReply = action.remoteInputs?.any { it.allowFreeFormInput && it.allowedDataTypes?.contains("text/plain") != false } == true
                    add(
                            TimelineItem.Action(
                                    (MetaNotificationAction.metaActionLength + index).toUByte(),
                                    if (isReply) TimelineItem.Action.Type.Response else TimelineItem.Action.Type.Generic,
                                    listOf(TimelineAttributeFactory.title(action.title.toString()))
                            )
                    )
                }
                add(
                        TimelineItem.Action(
                                MetaNotificationAction.Open.ordinal.toUByte(),
                                TimelineItem.Action.Type.Generic,
                                listOf(TimelineAttributeFactory.title("Open on phone"))
                        )
                )
                add(
                        TimelineItem.Action(
                                MetaNotificationAction.MutePackage.ordinal.toUByte(),
                                TimelineItem.Action.Type.Generic,
                                listOf(TimelineAttributeFactory.title("Mute app"))
                        )
                )
                add(
                        TimelineItem.Action(
                                MetaNotificationAction.MuteChannel.ordinal.toUByte(),
                                TimelineItem.Action.Type.Generic,
                                listOf(TimelineAttributeFactory.title("Mute channel\n'${tagId ?: ""}'"))
                        )
                )
            }
            activeNotifsState.value += (itemId to sbn)

            val notificationItem = TimelineItem(
                    itemId,
                    notificationsWatchappId,
                    sbn.postTime.milliseconds.inWholeSeconds.toUInt(),
                    0u,
                    TimelineItem.Type.Notification,
                    TimelineItem.Flag.makeFlags(listOf(
                            TimelineItem.Flag.IS_VISIBLE
                    )),
                    attributes = attributes,
                    layout = TimelineItem.Layout.GenericNotification,
                    actions = pebbleActions,
            )

            val packet = BlobCommand.InsertCommand(
                    Random.nextInt(0, UShort.MAX_VALUE.toInt()).toUShort(),
                    BlobCommand.BlobDatabase.Notification,
                    SUUID(StructMapper(), itemId).toBytes(),
                    notificationItem.toBytes(),
            )

            var result = blobDBService.send(packet)

            while (result.responseValue == BlobResponse.BlobStatus.TryLater) {
                Timber.w("BlobDB is busy, retrying in 1s")
                delay(1000)
                result = blobDBService.send(packet)
            }

            if (result.responseValue != BlobResponse.BlobStatus.Success) {
                Timber.e("Failed to send notification to Pebble, blobdb returned ${result.responseValue}")
            }
            persistedNotifDao.deleteOlderThan(System.currentTimeMillis() - 1.hours.inWholeMilliseconds)
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
            activeNotifsState.value.values.find {
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

        val result = displayActor.trySend(DisplayActorArgs(
                sbn.packageName,
                sbn.id.toLong(),
                tagId,
                title,
                text,
                notification.category ?: "",
                notification.color,
                messages ?: listOf(),
                notification.actions?.toList() ?: listOf(),
                sbn
        ))
        if (result.isFailure) {
            Timber.e(result.exceptionOrNull(), "Failed to send notification to display actor")
        }
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