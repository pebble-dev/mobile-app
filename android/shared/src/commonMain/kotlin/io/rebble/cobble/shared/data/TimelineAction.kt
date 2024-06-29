package io.rebble.cobble.shared.data

import io.rebble.libpebblecommon.packets.blobdb.TimelineAction
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import kotlinx.serialization.Serializable

@Serializable
data class TimelineAction(
        val actionId: Int,
        val actionType: TimelineItem.Action.Type,
        val attributes: List<TimelineAttribute>
) {
    fun toProtocolAction(): TimelineItem.Action {
        return TimelineItem.Action(
                actionId.toUByte(),
                actionType,
                attributes.map { it.toProtocolAttribute() }
        )
    }
}