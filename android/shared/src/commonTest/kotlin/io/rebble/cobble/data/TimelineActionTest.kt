package io.rebble.cobble.data

import io.rebble.cobble.shared.data.TimelineAction
import io.rebble.libpebblecommon.packets.blobdb.TimelineItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class TimelineActionTest {
    @Test
    fun test() {
        val action = TimelineAction(1, TimelineItem.Action.Type.Generic, emptyList())
        val json = Json.encodeToString(action)
        println(json)
        val deserialized = Json.decodeFromString(TimelineAction.serializer(), json)
        println(deserialized)
        assertEquals(action, deserialized)
    }
}