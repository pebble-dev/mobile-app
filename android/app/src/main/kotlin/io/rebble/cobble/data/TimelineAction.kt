package io.rebble.cobble.data

import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import kotlinx.serialization.Serializable

@Serializable
data class TimelineAction(
        val actionId: Int,
        val actionType: Int,
        val attributes: List<TimelineAttribute>
) {
    fun toProtocolAction(): TimelineItem.Action {
        return TimelineItem.Action(
                actionId.toUByte(),
                TimelineItem.Action.Type.values().firstOrNull {
                    it.value == actionType.toUByte()
                } ?: error("Unknown action type $actionType"),
                attributes.map { it.toProtocolAttribute() }
        )
    }
}