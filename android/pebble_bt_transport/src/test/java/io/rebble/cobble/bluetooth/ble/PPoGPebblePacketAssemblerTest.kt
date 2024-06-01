package io.rebble.cobble.bluetooth.ble

import io.rebble.cobble.bluetooth.ble.util.chunked
import io.rebble.libpebblecommon.packets.PingPong
import io.rebble.libpebblecommon.packets.PutBytesCommand
import io.rebble.libpebblecommon.packets.PutBytesPut
import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import io.rebble.libpebblecommon.protocolhelpers.ProtocolEndpoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalCoroutinesApi::class)
class PPoGPebblePacketAssemblerTest {
    @Test
    fun `Assemble small packet`() = runTest {
        val assembler = PPoGPebblePacketAssembler()
        val actualPacket = PingPong.Ping(2u).serialize().asByteArray()

        val results: MutableList<ByteArray> = mutableListOf()
        assembler.assemble(actualPacket).onEach {
            results.add(it)
        }.launchIn(this)
        runCurrent()
        val resultPacket = PebblePacket.deserialize(results[0].asUByteArray())

        assertEquals(1, results.size)
        assertTrue("Packet is incorrect type", resultPacket is PingPong.Ping)
        assertEquals(2u, (resultPacket as PingPong.Ping).cookie.get())
        assertArrayEquals(actualPacket, results[0])
    }

    @Test
    fun `Assemble large packet`() = runTest {
        val assembler = PPoGPebblePacketAssembler()
        val actualPacket = PutBytesPut(2u, UByteArray(1000)).serialize().asByteArray()
        val actualPackets = actualPacket.chunked(200)

        val results: MutableList<ByteArray> = mutableListOf()
        launch {
            for (packet in actualPackets) {
                assembler.assemble(packet).collect {
                    results.add(it)
                }
            }
        }
        runCurrent()

        val resultPacket = PebblePacket.deserialize(results[0].asUByteArray())
        assertEquals(1, results.size)
        assertEquals(ProtocolEndpoint.PUT_BYTES.value, resultPacket.endpoint.value)
        assertArrayEquals(actualPacket, results[0])
    }

    @Test
    fun `Assemble multiple packets`() = runTest {
        val assembler = PPoGPebblePacketAssembler()
        val actualPacketA = PingPong.Ping(2u).serialize().asByteArray()
        val actualPacketB = PutBytesPut(2u, UByteArray(1000)).serialize().asByteArray()
        val actualPacketC = PingPong.Pong(3u).serialize().asByteArray()
        val actualPackets = actualPacketA + actualPacketB + actualPacketC

        val results: MutableList<ByteArray> = mutableListOf()
        assembler.assemble(actualPackets).onEach {
            results.add(it)
        }.launchIn(this)
        runCurrent()

        val resultPackets = results.map { PebblePacket.deserialize(it.asUByteArray()) }

        assertEquals(3, results.size)

        assertTrue(resultPackets[0] is PingPong.Ping)
        assertEquals(2u, (resultPackets[0] as PingPong.Ping).cookie.get())
        assertArrayEquals(actualPacketA, results[0])

        assertEquals(ProtocolEndpoint.PUT_BYTES.value, resultPackets[1].endpoint.value)
        assertArrayEquals(actualPacketB, results[1])

        assertTrue(resultPackets[2] is PingPong.Pong)
        assertEquals(3u, (resultPackets[2] as PingPong.Pong).cookie.get())
        assertArrayEquals(actualPacketC, results[2])
    }
}