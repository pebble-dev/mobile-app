package io.rebble.cobble.notifications

import android.content.Context
import io.rebble.cobble.shared.domain.notifications.MetaNotificationAction
import io.rebble.cobble.shared.domain.notifications.PlatformNotificationActionExecutor
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import io.rebble.libpebblecommon.services.blobdb.TimelineService

class AndroidNotificationActionExecutor(
        private val context: Context
): PlatformNotificationActionExecutor {

    override suspend fun handleMetaNotificationAction(action: MetaNotificationAction, itemId: Uuid, attributes: List<TimelineItem.Attribute>): TimelineService.ActionResponse {
        return when (action) {
            MetaNotificationAction.Dismiss -> TODO()
            MetaNotificationAction.Open -> TODO()
            MetaNotificationAction.MutePackage -> TODO()
            MetaNotificationAction.MuteChannel -> TODO()
        }
    }

    override suspend fun handlePlatformAction(actionId: Int, itemId: Uuid, attributes: List<TimelineItem.Attribute>): TimelineService.ActionResponse {
        TODO("Not yet implemented")
    }
}