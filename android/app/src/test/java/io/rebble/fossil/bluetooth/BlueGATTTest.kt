package io.rebble.fossil.bluetooth

import org.junit.Assert.assertArrayEquals
import org.junit.Test
import kotlin.random.Random

internal class BlueGATTTest {
    val random = Random.Default

    @Test
    fun largeSplitPacketWrite() {
        val mtu = 30
        val payload = random.nextBytes(mtu * 8)
        val splitPayload = BlueLEDriver.splitBytesByMTU(payload, mtu)
        val packets = mutableListOf<GATTPacket>()
        splitPayload.forEach {
            packets.add(GATTPacket(GATTPacket.PacketType.DATA, 10U, it))
        }

        var buf = byteArrayOf()
        packets.forEach {
            buf += it.data.copyOfRange(1, it.data.size)
        }
        assertArrayEquals("Payload changed", payload, buf)
    }
}