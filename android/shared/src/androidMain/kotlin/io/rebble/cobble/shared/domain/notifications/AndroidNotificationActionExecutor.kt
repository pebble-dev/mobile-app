package io.rebble.cobble.shared.domain.notifications

import android.app.ActivityOptions
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.database.dao.NotificationChannelDao
import io.rebble.cobble.shared.datastore.KMPPrefs
import io.rebble.libpebblecommon.packets.blobdb.TimelineAttribute
import io.rebble.libpebblecommon.packets.blobdb.TimelineIcon
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import io.rebble.libpebblecommon.services.blobdb.TimelineService
import io.rebble.libpebblecommon.util.TimelineAttributeFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.UUID

class AndroidNotificationActionExecutor(): PlatformNotificationActionExecutor, KoinComponent {
    private val context: Context by inject()
    private val activeNotifsState: MutableStateFlow<Map<UUID, StatusBarNotification>> by inject(named("activeNotifsState"))
    private val prefs: KMPPrefs by inject()
    private val channelDao: NotificationChannelDao by inject()
    private val activityOptions = ActivityOptions.makeBasic().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
        }
    }

    override suspend fun handleMetaNotificationAction(action: MetaNotificationAction, itemId: Uuid, attributes: List<TimelineItem.Attribute>): TimelineService.ActionResponse {
        val sbn = activeNotifsState.value[itemId]
                ?: run {
                    Logging.w("Notification not found for action, could be notif before we started tracking them")
                    return TimelineService.ActionResponse(success = false)
                }
        return when (action) {
            MetaNotificationAction.Dismiss -> actionIntent(sbn.notification.deleteIntent)
                    .map {
                        TimelineService.ActionResponse(
                                success = true,
                                attributes = listOf(
                                        TimelineAttributeFactory.subtitle("Dismissed"),
                                        TimelineAttributeFactory.largeIcon(TimelineIcon.ResultDismissed),
                                )
                        )
                    }
                    .catch {
                        if (prefs.sensitiveDataLoggingEnabled.first()) {
                            Logging.e("Error while sending notification action intent", it)
                        }
                        emit(TimelineService.ActionResponse(success = false))
                    }
                    .first()
            MetaNotificationAction.Open -> actionIntent(sbn.notification.contentIntent)
                    .map {
                        TimelineService.ActionResponse(
                                success = true,
                                attributes = listOf(
                                        TimelineAttributeFactory.subtitle("Opened on phone"),
                                        TimelineAttributeFactory.largeIcon(TimelineIcon.DuringPhoneCall),
                                )
                        )
                    }
                    .catch {
                        if (prefs.sensitiveDataLoggingEnabled.first()) {
                            Logging.e("Error while sending notification action intent", it)
                        }
                        emit(TimelineService.ActionResponse(success = false))
                    }
                    .first()
            MetaNotificationAction.MutePackage -> {
                prefs.setMutedPackages(prefs.mutedPackages.first() + sbn.packageName)
                TimelineService.ActionResponse(
                        success = true,
                        attributes = listOf(
                                TimelineAttributeFactory.subtitle("Muted app"),
                                TimelineAttributeFactory.largeIcon(TimelineIcon.ResultMute),
                        )
                )
            }
            MetaNotificationAction.MuteChannel -> {
                channelDao.get(sbn.packageName, sbn.notification.channelId)?.let {
                    channelDao.setShouldNotify(sbn.packageName, sbn.notification.channelId, false)
                    TimelineService.ActionResponse(
                            success = true,
                            attributes = listOf(
                                    TimelineAttributeFactory.subtitle("Muted channel"),
                                    TimelineAttributeFactory.largeIcon(TimelineIcon.ResultMute),
                            )
                    )
                } ?: TimelineService.ActionResponse(success = false)
            }
        }
    }

    override suspend fun handlePlatformAction(actionId: Int, itemId: Uuid, attributes: List<TimelineItem.Attribute>): TimelineService.ActionResponse {
        val sbn = activeNotifsState.value[itemId]
                ?: return TimelineService.ActionResponse(success = false)
        val actions = sbn.notification.actions ?: return TimelineService.ActionResponse(success = false)
        val action = actions.getOrNull(actionId-MetaNotificationAction.metaActionLength)
                ?: return TimelineService.ActionResponse(success = false)

        val fillIntent = if (action.isReply) {
            val replyText = attributes.firstOrNull { it.attributeId.get() == TimelineAttribute.Title.id }
                    ?.content?.get()?.asByteArray()?.decodeToString()
                    ?: return TimelineService.ActionResponse(success = false)
            val replyInput = action.replyInput ?: return TimelineService.ActionResponse(success = false)
            val fillIntent = Intent()
            RemoteInput.addResultsToIntent(arrayOf(replyInput), fillIntent, Bundle().apply {
                putString(replyInput.resultKey, replyText)
            })
            fillIntent
        } else {
            null
        }

        val successResponse = if (action.isReply) {
            TimelineService.ActionResponse(
                    success = true,
                    attributes = listOf(
                            TimelineAttributeFactory.subtitle("Replied"),
                            TimelineAttributeFactory.largeIcon(TimelineIcon.ResultSent),
                    )
            )
        } else {
            TimelineService.ActionResponse(
                    success = true,
                    attributes = listOf(
                            TimelineAttributeFactory.subtitle("Done"),
                            TimelineAttributeFactory.largeIcon(TimelineIcon.GenericConfirmation),
                    )
            )
        }

        return actionIntent(action.actionIntent, fillIntent)
                .map {
                    successResponse
                }
                .catch {
                    if (prefs.sensitiveDataLoggingEnabled.first()) {
                        Logging.e("Error while sending notification action intent", it)
                    }
                    emit(TimelineService.ActionResponse(success = false))
                }
                .first()
    }

    private suspend fun actionIntent(intent: PendingIntent, fillintent: Intent? = null) = callbackFlow {
        val sensitiveLogging = prefs.sensitiveDataLoggingEnabled.first()
        val callback = PendingIntent.OnFinished { pendingIntent, intent, resultCode, resultData, resultExtras ->
            if (sensitiveLogging) {
                Logging.d("Intent sent: $intent, resultCode: $resultCode, resultData: $resultData, resultExtras: $resultExtras")
            }
            trySend(Unit)
        }
        intent.send(context, 0, fillintent, callback, null, null, activityOptions.toBundle())
        awaitClose {}
    }
}