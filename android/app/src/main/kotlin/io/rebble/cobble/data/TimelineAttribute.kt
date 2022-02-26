package io.rebble.cobble.data

import io.rebble.cobble.util.encodeToByteArrayTrimmed
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import io.rebble.libpebblecommon.structmapper.SUInt
import io.rebble.libpebblecommon.structmapper.StructMapper
import kotlinx.serialization.Serializable
import java.nio.ByteBuffer

@Serializable
data class TimelineAttribute(
        val id: Int,
        val string: String? = null,
        val listOfString: List<String>? = null,
        val uint8: Int? = null,
        val uint32: Long? = null,
        val maxLength: Int = 64
) {
    fun toProtocolAttribute(): TimelineItem.Attribute {
        val content: UByteArray = when {
            string != null -> {
                string.encodeToByteArrayTrimmed(maxLength)
                        .toUByteArray()
            }
            listOfString != null -> {
                val combinedStrings = listOfString.joinToString(separator = "\u0000")
                combinedStrings.encodeToByteArrayTrimmed(maxLength).toUByteArray()
            }
            uint8 != null -> {
                ubyteArrayOf(uint8.toUByte())
            }
            uint32 != null -> {
                SUInt(StructMapper(), uint32.toUInt(), '<').toBytes()
            }
            else -> throw IllegalArgumentException("Received empty timeline attribute: $this")
        }

        return TimelineItem.Attribute(
                id.toUByte(),
                content
        )
    }

    companion object {
        fun fromProtocolAttribute(attr: TimelineItem.Attribute): TimelineAttribute {
            return when (val id = attr.attributeId.get().toInt()) {
                in 1..3, 9, 11, 12, in 15..22, 24 -> {
                    TimelineAttribute(id, string = String(attr.content.get().toByteArray()))
                }

                in 4..7, 13, 14 -> {
                    TimelineAttribute(id, uint32 = ByteBuffer.wrap(attr.content.get().toByteArray()).getLong(0))
                }

                else -> {
                    throw NotImplementedError("Attr ID $id does not have an implemented type.")
                }
            }
        }
    }
}