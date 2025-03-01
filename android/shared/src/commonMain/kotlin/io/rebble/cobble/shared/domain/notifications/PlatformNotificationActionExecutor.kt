package io.rebble.cobble.shared.domain.notifications

import com.benasher44.uuid.Uuid
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import io.rebble.libpebblecommon.services.blobdb.TimelineService

interface PlatformNotificationActionExecutor {
    suspend fun handleMetaNotificationAction(
        action: MetaNotificationAction,
        itemId: Uuid,
        attributes: List<TimelineItem.Attribute>
    ): TimelineService.ActionResponse

    suspend fun handlePlatformAction(
        actionId: Int,
        itemId: Uuid,
        attributes: List<TimelineItem.Attribute>
    ): TimelineService.ActionResponse
}