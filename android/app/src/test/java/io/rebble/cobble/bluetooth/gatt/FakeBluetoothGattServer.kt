package io.rebble.cobble.bluetooth.gatt

import io.rebble.libpebblecommon.ble.GATTPacket
import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.junit.Assert.assertEquals

class FakePPoGATTServer : PPoGATTServer {
    private val packetsChannel = Channel<GATTPacket>(Channel.BUFFERED)
    private val mtuChannel = Channel<Int>(Channel.BUFFERED)

    override val currentMtu: Flow<Int> = mtuChannel.receiveAsFlow()
    override val packetsFromWatch: Flow<GATTPacket> = packetsChannel.receiveAsFlow()

    val packetsSentToWatch = ArrayList<GATTPacket>()

    override suspend fun sendToWatch(packet: GATTPacket) {
        packetsSentToWatch += packet
    }

    fun emitMtu(mtu: Int) {
        mtuChannel.trySend(mtu).getOrThrow()
    }

    fun emitPacketFromWatch(packet: GATTPacket) {
        packetsChannel.trySend(packet).getOrThrow()
    }

    fun emitPacketFromWatch(packet: PebblePacket, sequence: Int = 0) {
        emitPacketFromWatch(
                GATTPacket(GATTPacket.PacketType.DATA, sequence, packet.serialize().asByteArray())
        )
    }

    fun assertPacketsSentToWatch(vararg expectedPackets: GATTPacket) {
        assert(packetsSentToWatch.size == expectedPackets.size) {
            "Should have received ${expectedPackets.size} packets," +
                    " received ${expectedPackets.contentToString()} instead"
        }

        for (i in expectedPackets.indices) {
            val expectedPacket = expectedPackets[i]
            val actualPacket = packetsSentToWatch[i]

            assertEquals("Packet $i does not match. Expected: '$expectedPacket', got '$actualPacket' instead", expectedPacket, actualPacket)
        }
    }
}