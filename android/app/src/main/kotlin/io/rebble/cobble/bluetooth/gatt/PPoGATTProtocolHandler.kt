package io.rebble.cobble.bluetooth.gatt

import io.rebble.libpebblecommon.ble.GATTPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import kotlin.math.min

class PPoGATTProtocolHandler(scope: CoroutineScope, private val gattDriver: PPoGATTServer) {
    private val _rxPebblePacketFlow = MutableSharedFlow<ByteArray>()
    val rxPebblePacketFlow: SharedFlow<ByteArray> = _rxPebblePacketFlow

    inner class PendingACK(val sequence: Int, val success: Boolean)
    private val ackFlow = MutableSharedFlow<PendingACK>()

    val connectionStateChannel = Channel<Boolean>(Channel.UNLIMITED)

    private var initialReset = false
    private var initialData = false

    var connectionVersion = GATTPacket.PPoGConnectionVersion.ZERO
        private set
    var maxRXWindow: Byte = /*LEConstants.MAX_RX_WINDOW*/ 1
        private set
    var maxTXWindow: Byte = /*LEConstants.MAX_TX_WINDOW*/ 1
        private set
    private val seq = GATTSequence()
    private val remoteSeq = GATTSequence()

    private var pendingPacket: PendingPacket? = null

    var maxPacketSize = 25-4

    private val rxJob = scope.launch {
        gattDriver.packetRxFlow.collect {
            onPacket(it)
        }
    }

    private val mtuJob = scope.launch {
        gattDriver.mtuFlow.collect {
            maxPacketSize = it-4
        }
    }

    private suspend fun onPacket(packet: GATTPacket) {
        try {
            withTimeout(1000) {
                when (packet.type) {
                    GATTPacket.PacketType.DATA -> onData(packet)
                    GATTPacket.PacketType.ACK -> onACK(packet)
                    GATTPacket.PacketType.RESET -> onReset(packet)
                    GATTPacket.PacketType.RESET_ACK -> onResetAck(packet)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception while processing packet")
        }
    }

    /* ==== RX ==== */

    private suspend fun onData(packet: GATTPacket) {
        Timber.d("-> DATA ${packet.sequence}")
        require(packet.data.size-1 > 0) {"Data packet with empty content invalid"}
        if (!initialReset) {
            Timber.w("Data before initial reset") //TODO: someday handle quick reconnection recovery?
            sendReset()
            return
        }
        if (!initialData) {
            initialData = true
            connectionStateChannel.trySend(true) // fully connected
        }
        val cRemoteSeq = remoteSeq.next
        if (packet.sequence == cRemoteSeq) {
            var protoData = packet.toByteArray().drop(1)
            while (protoData.isNotEmpty()) {
                if (pendingPacket == null) {
                    pendingPacket = PendingPacket()
                }
                val added = pendingPacket!!.addData(protoData)
                protoData = protoData.drop(added)
                if (pendingPacket!!.isComplete) {
                    _rxPebblePacketFlow.emit(pendingPacket!!.data.toByteArray())
                    pendingPacket = null
                }
            }
            sendAck(packet.sequence)
        } else {
            Timber.w("Unexpected sequence ${packet.sequence}, expected $cRemoteSeq")
            //TODO: recover by sending ACK rewind
            sendReset()
        }
    }

    private suspend fun onACK(packet: GATTPacket) {
        Timber.d("-> ACK ${packet.sequence}")
        if (packet.sequence < seq.current-1) {
            Timber.e("ACK for previous packet") //TODO: handle ACK rewind
            sendReset()
            ackFlow.emit(PendingACK(packet.sequence, false))
        } else {
            ackFlow.emit(PendingACK(packet.sequence, true))
        }
    }

    private suspend fun onReset(packet: GATTPacket) {
        Timber.d("-> RESET ${packet.sequence}")
        if (seq.current != 0) {
            Timber.e("Got reset on non zero sequence")
        }
        if (!initialReset) {
            initialReset = true
        }
        connectionVersion = packet.getPPoGConnectionVersion()
        sendResetAck()
    }

    private suspend fun onResetAck(packet: GATTPacket) {
        Timber.d("-> RESETACK ${packet.sequence}")
        Timber.d("Connection version ${connectionVersion.value}")
        if (connectionVersion.supportsWindowNegotiation && !packet.hasWindowSizes()) {
            Timber.w("FW does not support window sizes in reset complete, reverting to connectionVersion 0")
            connectionVersion = GATTPacket.PPoGConnectionVersion.ZERO
        }

        if (connectionVersion.supportsWindowNegotiation) {
            maxRXWindow = min(packet.getMaxRXWindow().toInt(), /*LEConstants.MAX_RX_WINDOW.toInt()*/ 1).toByte()
            maxTXWindow = min(packet.getMaxTXWindow().toInt(), /*LEConstants.MAX_TX_WINDOW.toInt()*/ 1).toByte()
            Timber.i("Windows negotiated: rx = $maxRXWindow, tx = $maxTXWindow")
        }
        sendResetAck()
        if (!initialReset) {
            Timber.i("Initial reset, everything is connected now")
        }
    }

    /* ==== TX ==== */

    private suspend fun sendResetAck() {
        val data = if (connectionVersion.supportsWindowNegotiation) {
            byteArrayOf(maxRXWindow, maxTXWindow)
        } else {
            null
        }
        reset()
        writePacket(GATTPacket.PacketType.RESET_ACK, data, 0)
    }

    private suspend fun sendAck(sequence: Int) {
        writePacket(GATTPacket.PacketType.ACK, null, sequence) //TODO: coalesced ACK
    }

    private suspend fun sendReset() {
        writePacket(GATTPacket.PacketType.RESET, null, 0)
    }

    suspend fun sendPebblePacket(data: ByteArray): Boolean {
        val chunks = data.toList().chunked(maxPacketSize)
        for (chunk in chunks) {
            val txSequence = writePacket(GATTPacket.PacketType.DATA, chunk.toByteArray()).sequence
            try {
                val success = withTimeout(5000) {
                    val ack = ackFlow.first { it.sequence == txSequence }
                    return@withTimeout ack.success
                }
                if (!success) {
                    return false
                }
            } catch (e: TimeoutCancellationException) {
                Timber.e("Timed out waiting for ACK $txSequence sending protocol packet")
                return false
            }
        }
        return true
    }

    private suspend fun writePacket(type: GATTPacket.PacketType, data: ByteArray?, sequence: Int? = null): GATTPacket {
        val packet = GATTPacket(type, sequence ?: seq.next, data?.let {
            return@let if (it.isNotEmpty()) {
                it
            } else {
                null
            }
        })

        when (type) {
            GATTPacket.PacketType.DATA -> {
                Timber.d("<- DATA ${packet.sequence}")
                require(data != null && data.size <= maxPacketSize+4)
            }
            GATTPacket.PacketType.RESET -> {
                Timber.d("<- RESET ${packet.sequence}")
            }
            GATTPacket.PacketType.RESET_ACK -> {
                Timber.d("<- RESETACK ${packet.sequence}")
            }
            GATTPacket.PacketType.ACK -> {
                Timber.d("<- ACK ${packet.sequence}")
            }
            else -> throw IllegalArgumentException()
        }
        gattDriver.write(packet.data)
        return packet
    }

    private fun reset() {
        Timber.i("Resetting LE")
        remoteSeq.reset()
        seq.reset()
        pendingPacket = null
    }

    override fun close() {
        rxJob.cancel()
        mtuJob.cancel()
    }
}