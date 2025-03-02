package io.rebble.cobble.shared.data

import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class TimelineAction(
    val actionId: Int,
    @Serializable(with = TimelineActionTypeSerializer::class)
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

object TimelineActionTypeSerializer : KSerializer<TimelineItem.Action.Type> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("TimelineItemActionType", PrimitiveKind.INT)

    override fun serialize(
        encoder: Encoder,
        value: TimelineItem.Action.Type
    ) {
        encoder.encodeInt(value.value.toInt())
    }

    override fun deserialize(decoder: Decoder): TimelineItem.Action.Type {
        val raw = decoder.decodeInt()
        return TimelineItem.Action.Type.entries.first { it.value.toInt() == raw }
    }
}