package io.rebble.cobble.bluetooth

import io.rebble.libpebblecommon.ble.GATTPacket
import io.rebble.libpebblecommon.packets.blobdb.PushNotification
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

internal class GATTPacketTest {
    val payload =
        PushNotification(
            "Test",
            "Test Sender",
            "A quite long message for the notification ........ ................"
        )

    @Test
    fun generateValidPacket() {
        val gattPacket =
            GATTPacket(GATTPacket.PacketType.DATA, 16, payload.serialize().toByteArray())

        val expected = byteArrayOf(0b10000000.toByte()) + payload.serialize().toByteArray()
        val actual = gattPacket.toByteArray()

        assertArrayEquals(expected, actual)
    }

    @Test
    fun decodeValidPacket() {
        val expectedType = GATTPacket.PacketType.DATA
        val expectedSeq = 16
        val expectedPayload = payload.serialize().toByteArray()

        val gattPacket = GATTPacket(byteArrayOf(0b10000000.toByte()) + expectedPayload)
        assertEquals(expectedSeq, gattPacket.sequence)
        assertEquals(expectedType, gattPacket.type)
        assertArrayEquals(
            expectedPayload,
            gattPacket.data.slice(1..<gattPacket.data.size).toByteArray()
        )
    }
}