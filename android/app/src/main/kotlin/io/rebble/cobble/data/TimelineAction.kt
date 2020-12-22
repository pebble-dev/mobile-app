package io.rebble.cobble.data

import com.squareup.moshi.JsonClass
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem

@JsonClass(generateAdapter = true)
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