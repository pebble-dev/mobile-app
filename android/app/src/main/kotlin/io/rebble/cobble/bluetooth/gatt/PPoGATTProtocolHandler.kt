package io.rebble.cobble.bluetooth.gatt

import androidx.annotation.RequiresPermission
import io.rebble.libpebblecommon.ble.GATTPacket
import io.rebble.libpebblecommon.util.DataBuffer
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import kotlin.math.min

class PPoGATTProtocolHandler(scope: CoroutineScope, private val gattDriver: PPoGATTServer) {
    private val _rxPebblePacketFlow = MutableSharedFlow<ByteArray>()
    val rxPebblePacketFlow: SharedFlow<ByteArray> = _rxPebblePacketFlow
    val txPebblePacketFlow = MutableSharedFlow<ByteArray>(extraBufferCapacity = 4)

    inner class PendingACK(val sequence: Int, val success: Boolean)
    private val ackFlow = MutableSharedFlow<PendingACK>()

    val connectionStateChannel = Channel<Boolean>(Channel.UNLIMITED)

    private var initialReset = false
    private var initialData = false

    private var connectionVersion = GATTPacket.PPoGConnectionVersion.ZERO
    private var maxRXWindow: Byte = /*LEConstants.MAX_RX_WINDOW*/ 1
    private var maxTXWindow: Byte = /*LEConstants.MAX_TX_WINDOW*/ 1
    private val seq = GATTSequence()
    private val remoteSeq = GATTSequence()

    private var pendingPacket: PendingPacket? = null

    public var maxPacketSize = 25-4

    init {
        scope.launch {
            txPebblePacketFlow.collect {
                sendPebblePacket(it)
            }
        }

        scope.launch {
            gattDriver.packetRxFlow.collect {
                onPacket(it)
            }
        }

        scope.launch {
            gattDriver.mtuFlow.collect {
                maxPacketSize = it-4
            }
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
        } catch (e: TimeoutCancellationException) {
            Timber.e(e, "Timed out processing packet")
        }
    }

    /* ==== RX ==== */

    private suspend fun onData(packet: GATTPacket) {
        Timber.d("-> DATA ${packet.sequence}")
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
            sendAck(packet.sequence)
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
        writePacket(GATTPacket.PacketType.RESET_ACK, data, 0)
        reset()
    }

    private suspend fun sendAck(sequence: Int) {
        writePacket(GATTPacket.PacketType.ACK, null, sequence) //TODO: coalesced ACK
    }

    private suspend fun sendReset() {
        writePacket(GATTPacket.PacketType.RESET, null, 0)
    }

    private suspend fun sendPebblePacket(data: ByteArray) {
        var i = 0
        var done = 0
        while (done < data.size) {
            val chunk = data.slice(i*maxPacketSize until min((i*maxPacketSize)+maxPacketSize, done)).toByteArray()
            val txSequence = writePacket(GATTPacket.PacketType.DATA, chunk).sequence
            withTimeout(5000) {
                val ack = ackFlow.first { it.sequence == txSequence }
                check(ack.success)
            }
            i++
            done += chunk.size
        }
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
}