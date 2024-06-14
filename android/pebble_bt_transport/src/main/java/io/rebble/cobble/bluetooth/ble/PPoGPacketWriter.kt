package io.rebble.cobble.bluetooth.ble

import androidx.annotation.RequiresPermission
import io.rebble.libpebblecommon.ble.GATTPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.Closeable
import java.util.LinkedList

class PPoGPacketWriter(private val scope: CoroutineScope, private val stateManager: PPoGSession.StateManager, private val onTimeout: () -> Unit) : Closeable {
    private var metaWaitingToSend: GATTPacket? = null
    val dataWaitingToSend: LinkedList<GATTPacket> = LinkedList()
    val inflightPackets: LinkedList<GATTPacket> = LinkedList()
    var txWindow = 1
    private var timeoutJob: Job? = null
    private val _packetWriteFlow = MutableSharedFlow<GATTPacket>()
    val packetWriteFlow: SharedFlow<GATTPacket> = _packetWriteFlow
    private val packetSendStatusFlow = MutableSharedFlow<Pair<GATTPacket, Boolean>>()

    suspend fun setPacketSendStatus(packet: GATTPacket, status: Boolean) {
        packetSendStatusFlow.emit(Pair(packet, status))
    }

    suspend fun packetSendStatus(packet: GATTPacket): Boolean {
        return packetSendStatusFlow.first { it.first == packet }.second
    }

    companion object {
        private const val PACKET_ACK_TIMEOUT_MILLIS = 10_000L
    }

    suspend fun sendOrQueuePacket(packet: GATTPacket) {
        if (packet.type == GATTPacket.PacketType.DATA) {
            dataWaitingToSend.add(packet)
        } else {
            metaWaitingToSend = packet
        }
        sendNextPacket()
    }

    fun cancelTimeout() {
        timeoutJob?.cancel()
    }

    suspend fun onAck(packet: GATTPacket) {
        require(packet.type == GATTPacket.PacketType.ACK)
        if (packet.sequence < (dataWaitingToSend.lastOrNull()?.sequence ?: -1)) {
            Timber.w("Received rewind ACK")
            return
        }
        for (waitingPacket in dataWaitingToSend.iterator()) {
            if (waitingPacket.sequence == packet.sequence) {
                dataWaitingToSend.remove(waitingPacket)
                break
            }
        }
        if (inflightPackets.find { it.sequence == packet.sequence } == null) {
            Timber.w("Received ACK for packet not in flight")
            return
        }
        var ackedPacket: GATTPacket? = null

        // remove packets until the acked packet
        while (ackedPacket?.sequence != packet.sequence) {
            ackedPacket = inflightPackets.poll()
            check(ackedPacket != null) { "Polled inflightPackets to empty" }
        }
        sendNextPacket()
        rescheduleTimeout()
    }

    @Throws(SecurityException::class)
    suspend fun sendNextPacket() {
        if (metaWaitingToSend == null && dataWaitingToSend.isEmpty()) {
            return
        }

        val packet = if (metaWaitingToSend != null) {
            metaWaitingToSend
        } else {
            if (inflightPackets.size > txWindow) {
                return
            } else {
                dataWaitingToSend.peek()
            }
        }

        if (packet == null) {
            return
        }

        if (packet.type !in stateManager.state.allowedTxTypes) {
            Timber.e("Attempted to send packet of type ${packet.type} in state ${stateManager.state}")
            return
        }

        try {
            sendPacket(packet)
        } catch (e: Exception) {
            Timber.e(e, "Exception while sending packet")
            return
        }
        if (!packetSendStatus(packet)) {
            return
        }

        if (packet.type == GATTPacket.PacketType.DATA) {
            dataWaitingToSend.poll()
            inflightPackets.offer(packet)
        } else {
            metaWaitingToSend = null
        }

        rescheduleTimeout()

        sendNextPacket()
    }

    fun rescheduleTimeout(force: Boolean = false) {
        timeoutJob?.cancel()
        if (inflightPackets.isNotEmpty() || force) {
            timeoutJob = scope.launch {
                delay(PACKET_ACK_TIMEOUT_MILLIS)
                onTimeout()
            }
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private suspend fun sendPacket(packet: GATTPacket) {
        val data = packet.toByteArray()
        require(data.size <= (stateManager.mtuSize - 3)) { "Packet too large to send: ${data.size} > ${stateManager.mtuSize}-3" }
        _packetWriteFlow.emit(packet)
    }

    override fun close() {
        timeoutJob?.cancel()
    }
}