package io.rebble.cobble.notifications

import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.domain.common.SystemAppIDs.notificationsWatchappId
import io.rebble.cobble.shared.domain.notifications.MetaNotificationAction
import io.rebble.cobble.shared.domain.notifications.PlatformNotificationActionExecutor
import io.rebble.cobble.shared.domain.timeline.TimelineActionManager
import io.rebble.cobble.shared.handlers.CobbleHandler
import io.rebble.libpebblecommon.services.blobdb.TimelineService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NotificationActionHandler(scope: CoroutineScope ): KoinComponent, CobbleHandler {
    private val timelineActionManager: TimelineActionManager by inject()
    private val notificationActionExecutor: PlatformNotificationActionExecutor by inject()

    private val notificationActionFlow = timelineActionManager.actionFlowForApp(notificationsWatchappId)

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
                TimelineService.ActionResponse(
                        success = false
                )
            }
            deferred.complete(response)
        }.launchIn(scope)
    }
}