package io.rebble.fossil.bluetooth

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import java.nio.ByteBuffer
import kotlin.random.Random

internal class BlueGATTTest {
    val random = Random.Default

    fun largeSplitPacketWrite() {
        val mtu = 30
        val payload = random.nextBytes(mtu * 8)
        val splitPayload = BlueLEDriver.splitBytesByMTU(payload, mtu)
        val packets = mutableListOf<GATTPacket>()
        splitPayload.forEach {
            packets.add(GATTPacket(GATTPacket.PacketType.DATA, 10U, it))
        }

        val buf = ByteBuffer.allocate(mtu * 8)
        packets.forEach {
            buf.put(it.data.slice(1..it.data.size - 1).toByteArray())
        }
        assertFalse("Did not fill buffer", buf.hasRemaining())
        assertArrayEquals("Payload changed", payload, buf.array())
    }
}