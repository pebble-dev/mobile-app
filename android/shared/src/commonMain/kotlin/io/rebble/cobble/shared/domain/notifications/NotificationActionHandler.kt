package io.rebble.cobble.shared.domain.notifications

import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.domain.timeline.TimelineActionManager
import io.rebble.cobble.shared.handlers.CobbleHandler
import io.rebble.libpebblecommon.services.blobdb.TimelineService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NotificationActionHandler(scope: CoroutineScope): KoinComponent, CobbleHandler {
    companion object {
        const val NOTIFICATION_UUID_PREFIX = "cafecafe"
    }
    private val timelineActionManager: TimelineActionManager by inject()
    private val notificationActionExecutor: PlatformNotificationActionExecutor by inject()

    private val notificationActionFlow = timelineActionManager.actionFlow.filter {
        val (action, _) = it
        action.itemID.get().toString().startsWith(NOTIFICATION_UUID_PREFIX)
    }

    init {
        notificationActionFlow.onEach {
            val (action, deferred) = it
            val itemId = action.itemID.get()
            val response = try {
                if (action.actionID.get().toInt() < MetaNotificationAction.metaActionLength) {
                    notificationActionExecutor.handleMetaNotificationAction(
                            MetaNotificationAction.entries[action.actionID.get().toInt()],
                            itemId,
                            action.attributes.list
                    )
                } else {
                    notificationActionExecutor.handlePlatformAction(
                            action.actionID.get().toInt(),
                            itemId,
                            action.attributes.list
                    )
                }
            } catch (e: NoSuchElementException) {
                Logging.e("Error while handling notification action", e)
                TimelineService.ActionResponse(
                        success = false
                )
            }
            deferred.complete(response)
        }.catch {
            Logging.e("Error while handling notification action", it)
        }.launchIn(scope)
    }
}