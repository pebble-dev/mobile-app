package io.rebble.cobble.shared.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

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

val TimelinePin.isInPast: Boolean
    get() = if (isAllDay) {
        timestamp + 1.days < Clock.System.now()
    } else {
        timestamp + (duration?.minutes ?: 0.minutes) < Clock.System.now()
    }