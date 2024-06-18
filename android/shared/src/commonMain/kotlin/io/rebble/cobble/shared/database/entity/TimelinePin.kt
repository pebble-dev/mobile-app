package io.rebble.cobble.shared.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Entity
data class TimelinePin(
        /**
         * UUID of the item
         */
        @PrimaryKey val itemId: Uuid,
        /**
         * UUID of the watchapp that owns this item
         */
        val parentId: Uuid,
        /**
         * ID of the item that backs this item e.g. a calendar event ID
         */
        val backingId: String?,
        /**
         * Date of pin start
         */
        val timestamp: Instant,
        /**
         * Duration in minutes
         */
        val duration: Int?,
        val type: TimelineItem.Type,
        /**
         * Doesn't seem to be used
         */
        val isVisible: Boolean = true,
        /**
         * Display pin in UTC timezone
         */
        val isFloating: Boolean = false,
        /**
         * Ignores duration, just displays the pin for the whole day
         */
        val isAllDay: Boolean = false,
        /**
         * Shows the event in quick view while active
         */
        val persistQuickView: Boolean = false,
        val layout: TimelineItem.Layout,
        /**
         * JSON of timeline pin attributes, corresponding to the [layout]
         */
        val attributesJson: String?,
        /**
         * JSON of timeline pin actions, null if no actions
         */
        val actionsJson: String?,
        val nextSyncAction: NextSyncAction?
)

fun TimelinePin.toBlobData(): TimelineItem {
    val parsedAttributes: List<TimelineItem.Attribute> = attributesJson?.let { Json.decodeFromString(it) }
            ?: emptyList()

    val parsedActions: List<TimelineItem.Action> = actionsJson?.let { Json.decodeFromString(it) }
            ?: emptyList()
    return TimelineItem(
            itemId = itemId,
            parentId = parentId,
            timestamp = timestamp.toEpochMilliseconds().toUInt(),
            duration = (duration ?: 0).toUShort(),
            type = type,
            flags = TimelineItem.Flag.makeFlags(varsToFlags()),
            layout = layout,
            attributes = parsedAttributes,
            actions = parsedActions
    )
}

private fun TimelinePin.varsToFlags() = buildList {
    if (isVisible) {
        add(TimelineItem.Flag.IS_VISIBLE)
    }

    if (isFloating) {
        add(TimelineItem.Flag.IS_FLOATING)
    }

    if (isAllDay) {
        add(TimelineItem.Flag.IS_ALL_DAY)
    }

    if (persistQuickView) {
        add(TimelineItem.Flag.PERSIST_QUICK_VIEW)
    }
}