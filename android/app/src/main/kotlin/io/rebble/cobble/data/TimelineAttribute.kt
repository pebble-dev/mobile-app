package io.rebble.cobble.data

import com.squareup.moshi.JsonClass
import io.rebble.cobble.util.encodeToByteArrayTrimmed
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import io.rebble.libpebblecommon.structmapper.SInt
import io.rebble.libpebblecommon.structmapper.SUInt
import io.rebble.libpebblecommon.structmapper.StructMapper

@JsonClass(generateAdapter = true)
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
}