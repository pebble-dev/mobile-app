package io.rebble.cobble.shared.data

import io.rebble.cobble.shared.domain.common.Color
import io.rebble.cobble.shared.domain.common.toProtocolNumber
import io.rebble.cobble.shared.domain.timeline.TimelineGameState
import io.rebble.cobble.shared.domain.timeline.TimelineIcon
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import io.rebble.libpebblecommon.structmapper.SUInt
import io.rebble.libpebblecommon.structmapper.StructMapper
import io.rebble.libpebblecommon.util.DataBuffer
import io.rebble.libpebblecommon.util.encodeToByteArrayTrimmed
import kotlinx.serialization.Serializable

@Serializable
@OptIn(ExperimentalUnsignedTypes::class)
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
                    TimelineAttribute(id, attr.content.get().toByteArray().joinToString())
                }

                in 4..7, 13, 14 -> {
                    TimelineAttribute(id, uint32 = DataBuffer(attr.content.get()).getULong().toLong())
                }

                else -> {
                    throw NotImplementedError("Attr ID $id does not have an implemented type.")
                }
            }
        }

        fun title(title: String?) = TimelineAttribute(1, string = title)
        fun subtitle(subtitle: String?) = TimelineAttribute(2, string = subtitle)
        fun body(body: String?) = TimelineAttribute(3, string = body)
        fun tinyIcon(icon: TimelineIcon) = TimelineAttribute(4, uint32 = icon.protocolValue.toLong() or 0x80000000)
        fun smallIcon(icon: TimelineIcon) = TimelineAttribute(5, uint32 = icon.protocolValue.toLong() or 0x80000000)
        fun largeIcon(icon: TimelineIcon) = TimelineAttribute(6, uint32 = icon.protocolValue.toLong() or 0x80000000)
        fun ancsAction(action: Int) = TimelineAttribute(7, uint32 = action.toLong())
        fun cannedResponse(responses: List<String>) = TimelineAttribute(8, listOfString = responses, maxLength = 512)
        fun shortTitle(shortTitle: String) = TimelineAttribute(9, string = shortTitle, maxLength = 64)
        fun locationName(locationName: String) = TimelineAttribute(11, string = locationName, maxLength = 64)
        fun sender(sender: String) = TimelineAttribute(12, string = sender, maxLength = 64)
        fun launchCode(launchCode: Int) = TimelineAttribute(13, uint32 = launchCode.toLong())
        fun lastUpdated(time: Long) = TimelineAttribute(14, uint32 = time)
        fun rankAway(rank: String) = TimelineAttribute(15, string = rank)
        fun rankHome(rank: String) = TimelineAttribute(16, string = rank)
        fun nameAway(name: String) = TimelineAttribute(17, string = name)
        fun nameHome(name: String) = TimelineAttribute(18, string = name)
        fun recordAway(record: String) = TimelineAttribute(19, string = record)
        fun recordHome(record: String) = TimelineAttribute(20, string = record)
        fun scoreAway(score: String) = TimelineAttribute(21, string = score)
        fun scoreHome(score: String) = TimelineAttribute(22, string = score)
        fun sportsGameState(state: TimelineGameState) = TimelineAttribute(23, uint8 = state.protocolValue)
        fun broadcaster(name: String) = TimelineAttribute(24, string = name)
        fun headings(headings: List<String>) = TimelineAttribute(25, listOfString = headings, maxLength = 128)
        fun paragraphs(paragraphs: List<String>) = TimelineAttribute(26, listOfString = paragraphs, maxLength = 1024)
        fun foregroundColor(color: Color) = TimelineAttribute(27, uint8 = color.toProtocolNumber())
        fun primaryColor(color: Color) = TimelineAttribute(28, uint8 = color.toProtocolNumber())
        fun secondaryColor(color: Color) = TimelineAttribute(29, uint8 = color.toProtocolNumber())
        fun displayRecurring(recurring: Boolean) = TimelineAttribute(31, uint8 = if (recurring) 1 else 0)
        fun shortSubtitle(shortSubtitle: String) = TimelineAttribute(36, string = shortSubtitle, maxLength = 64)
        fun timestamp(timestamp: Long) = TimelineAttribute(37, uint32 = timestamp)
        fun displayTime(displayTime: Boolean) = TimelineAttribute(38, uint8 = if (displayTime) 1 else 0)
        fun subtitleTemplateString(templateString: String) = TimelineAttribute(47, string = templateString, maxLength = 150)
        fun icon(icon: TimelineIcon) = TimelineAttribute(48, uint8 = icon.protocolValue)
    }
}