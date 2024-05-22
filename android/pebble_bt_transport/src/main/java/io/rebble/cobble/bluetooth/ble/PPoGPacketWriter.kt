package io.rebble.cobble.bluetooth.ble

import androidx.annotation.RequiresPermission
import io.rebble.libpebblecommon.ble.GATTPacket
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.cancel
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.io.Closeable
import java.util.LinkedList
import kotlin.jvm.Throws

class PPoGPacketWriter(private val scope: CoroutineScope, private val stateManager: PPoGSession.StateManager, private val onTimeout: () -> Unit): Closeable {
    private var metaWaitingToSend: GATTPacket? = null
    private val dataWaitingToSend: LinkedList<GATTPacket> = LinkedList()
    private val inflightPackets: LinkedList<GATTPacket> = LinkedList()
    var txWindow = 1
    private var timeoutJob: Job? = null
    private val _packetWriteFlow = MutableSharedFlow<GATTPacket>()
    val packetWriteFlow = _packetWriteFlow
    private val packetSendStatusFlow = MutableSharedFlow<Pair<GATTPacket, Boolean>>()

    suspend fun setPacketSendStatus(packet: GATTPacket, status: Boolean) {
        packetSendStatusFlow.emit(Pair(packet, status))
    }

    private suspend fun packetSendStatus(packet: GATTPacket): Boolean {
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
        for (waitingPacket in dataWaitingToSend.iterator()) {
            if (waitingPacket.sequence == packet.sequence) {
                dataWaitingToSend.remove(waitingPacket)
                break
            }
        }
        if (!inflightPackets.contains(packet)) {
            Timber.w("Received ACK for packet not in flight")
            return
        }
        var ackedPacket: GATTPacket? = null

        // remove packets until the acked packet
        while (ackedPacket != packet) {
            ackedPacket = inflightPackets.poll()
        }
        sendNextPacket()
        rescheduleTimeout()
    }

    @Throws(SecurityException::class)
    private suspend fun sendNextPacket() {
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

        sendPacket(packet)
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
        require(data.size > stateManager.mtuSize) {"Packet too large to send: ${data.size} > ${stateManager.mtuSize}"}
        _packetWriteFlow.emit(packet)
    }

    override fun close() {
        timeoutJob?.cancel()
    }
}