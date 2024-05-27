package io.rebble.cobble.bluetooth.ble

import io.rebble.cobble.bluetooth.ble.util.chunked
import io.rebble.libpebblecommon.ble.GATTPacket
import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.consumeAsFlow
import timber.log.Timber
import java.io.Closeable
import kotlin.math.min

class PPoGSession(private val scope: CoroutineScope, private val serviceConnection: PPoGServiceConnection, var mtu: Int): Closeable {
    class PPoGSessionException(message: String) : Exception(message)

    private val pendingPackets = mutableMapOf<Int, GATTPacket>()
    private var ppogVersion: GATTPacket.PPoGConnectionVersion = GATTPacket.PPoGConnectionVersion.ZERO

    private var rxWindow = 0
    private var packetsSinceLastAck = 0
    private var sequenceInCursor = 0
    private var sequenceOutCursor = 0
    private var lastAck: GATTPacket? = null
    private var delayedAckJob: Job? = null
    private var delayedNACKJob: Job? = null
    private var resetAckJob: Job? = null
    private var writerJob: Job? = null
    private var failedResetAttempts = 0
    private val pebblePacketAssembler = PPoGPebblePacketAssembler()

    private val rxPebblePacketChannel = Channel<PebblePacket>(Channel.BUFFERED)

    private val jobActor = scope.actor<suspend () -> Unit> {
        for (job in channel) {
            job()
        }
    }

    inner class StateManager {
        var state: State = State.Closed
        var mtuSize: Int get() = mtu
            set(value) {}
    }

    val stateManager = StateManager()
    private var packetWriter = makePacketWriter()

    companion object {
        private const val MAX_SEQUENCE = 32
        private const val COALESCED_ACK_DELAY_MS = 200L
        private const val OUT_OF_ORDER_MAX_DELAY_MS = 50L
        private const val MAX_FAILED_RESETS = 3
        private const val MAX_SUPPORTED_WINDOW_SIZE = 25
        private const val MAX_SUPPORTED_WINDOW_SIZE_V0 = 4
    }

    enum class State(val allowedRxTypes: List<GATTPacket.PacketType>, val allowedTxTypes: List<GATTPacket.PacketType>) {
        Closed(listOf(GATTPacket.PacketType.RESET), listOf(GATTPacket.PacketType.RESET_ACK)),
        AwaitingResetAck(listOf(GATTPacket.PacketType.RESET_ACK), listOf(GATTPacket.PacketType.RESET)),
        AwaitingResetAckRequested(listOf(GATTPacket.PacketType.RESET_ACK), listOf(GATTPacket.PacketType.RESET)),
        Open(listOf(GATTPacket.PacketType.RESET, GATTPacket.PacketType.ACK, GATTPacket.PacketType.DATA), listOf(GATTPacket.PacketType.ACK, GATTPacket.PacketType.DATA)),
    }

    private fun makePacketWriter(): PPoGPacketWriter {
        val writer = PPoGPacketWriter(scope, stateManager) { onTimeout() }
        writerJob = scope.launch {
            writer.packetWriteFlow.collect {
                packetWriter.setPacketSendStatus(it, serviceConnection.writeDataRaw(it.toByteArray()))
            }
        }
        return writer
    }

    suspend fun handleData(value: ByteArray) {
        val ppogPacket = GATTPacket(value)
        when (ppogPacket.type) {
            GATTPacket.PacketType.RESET -> onResetRequest(ppogPacket)
            GATTPacket.PacketType.RESET_ACK -> onResetAck(ppogPacket)
            GATTPacket.PacketType.ACK -> onAck(ppogPacket)
            GATTPacket.PacketType.DATA -> {
                pendingPackets[ppogPacket.sequence] = ppogPacket
                processDataQueue()
            }
        }
    }

    suspend fun sendData(data: ByteArray) {
        if (stateManager.state != State.Open) {
            throw PPoGSessionException("Session not open")
        }
        val dataChunks = data.chunked(stateManager.mtuSize - 3)
        for (chunk in dataChunks) {
            val packet = GATTPacket(GATTPacket.PacketType.DATA, sequenceOutCursor, data)
            packetWriter.sendOrQueuePacket(packet)
            sequenceOutCursor = incrementSequence(sequenceOutCursor)
        }
    }

    private suspend fun onResetRequest(packet: GATTPacket) {
        require(packet.type == GATTPacket.PacketType.RESET)
        if (packet.sequence != 0) {
            throw PPoGSessionException("Reset packet must have sequence 0")
        }
        val nwVersion = packet.getPPoGConnectionVersion()
        Timber.d("Reset requested, new PPoGATT version: $nwVersion")
        ppogVersion = nwVersion
        stateManager.state = State.AwaitingResetAck
        packetWriter.rescheduleTimeout(true)
        resetState()
        val resetAckPacket = makeResetAck(sequenceOutCursor, MAX_SUPPORTED_WINDOW_SIZE, MAX_SUPPORTED_WINDOW_SIZE, ppogVersion)
        sendResetAck(resetAckPacket)
    }

    private fun makeResetAck(sequence: Int, rxWindow: Int, txWindow: Int, ppogVersion: GATTPacket.PPoGConnectionVersion): GATTPacket {
        return GATTPacket(GATTPacket.PacketType.RESET_ACK, sequence, if (ppogVersion.supportsWindowNegotiation) {
            byteArrayOf(rxWindow.toByte(), txWindow.toByte())
        } else {
            null
        })
    }

    private suspend fun sendResetAck(packet: GATTPacket) {
        val job = scope.launch(start = CoroutineStart.LAZY) {
            packetWriter.sendOrQueuePacket(packet)
        }
        resetAckJob = job
        jobActor.send {
            job.start()
            try {
                job.join()
            } catch (e: CancellationException) {
                Timber.v("Reset ACK job cancelled")
            }
        }
    }

    private suspend fun onResetAck(packet: GATTPacket) {
        require(packet.type == GATTPacket.PacketType.RESET_ACK)
        if (packet.sequence != 0) {
            throw PPoGSessionException("Reset ACK packet must have sequence 0")
        }
        if (stateManager.state == State.AwaitingResetAckRequested) {
            packetWriter.sendOrQueuePacket(makeResetAck(0, MAX_SUPPORTED_WINDOW_SIZE, MAX_SUPPORTED_WINDOW_SIZE, ppogVersion))
        }
        packetWriter.cancelTimeout()
        lastAck = null
        failedResetAttempts = 0

        if (ppogVersion.supportsWindowNegotiation && !packet.hasWindowSizes()) {
            Timber.i("FW claimed PPoGATT V1+ but did not send window sizes, reverting to V0")
            ppogVersion = GATTPacket.PPoGConnectionVersion.ZERO
        }
        Timber.d("Link established, PPoGATT version: ${ppogVersion}")
        if (!ppogVersion.supportsWindowNegotiation) {
            rxWindow = MAX_SUPPORTED_WINDOW_SIZE_V0
        } else {
            rxWindow = min(packet.getMaxRXWindow().toInt(), MAX_SUPPORTED_WINDOW_SIZE)
            packetWriter.txWindow = packet.getMaxTXWindow().toInt()
        }
        stateManager.state = State.Open
        PPoGLinkStateManager.updateState(serviceConnection.device.address, PPoGLinkState.SessionOpen)
    }

    private suspend fun onAck(packet: GATTPacket) {
        require(packet.type == GATTPacket.PacketType.ACK)
        packetWriter.onAck(packet)
    }

    private fun incrementSequence(sequence: Int): Int {
        return (sequence + 1) % MAX_SEQUENCE
    }

    private suspend fun ack(sequence: Int) {
        lastAck = GATTPacket(GATTPacket.PacketType.ACK, sequence)
        if (!ppogVersion.supportsCoalescedAcking) {
            sendAckCancelling()
            return
        }
        if (++packetsSinceLastAck >= (rxWindow / 2)) {
            sendAckCancelling()
            return
        }
        // We want to coalesce acks
        scheduleDelayedAck()
    }

    private suspend fun scheduleDelayedAck() {
        delayedAckJob?.cancel()
        val job = scope.launch(start = CoroutineStart.LAZY) {
            delay(COALESCED_ACK_DELAY_MS)
            sendAck()
        }
        delayedAckJob = job
        jobActor.send {
            job.start()
            try {
                job.join()
            } catch (e: CancellationException) {
                Timber.v("Delayed ACK job cancelled")
            }
        }
    }

    /**
     * Send an ACK cancelling the delayed ACK job if present
     */
    private suspend fun sendAckCancelling() {
        delayedAckJob?.cancel()
        sendAck()
    }

    /**
     * Send the last ACK packet
     */
    private suspend fun sendAck() {
        // Send ack
        lastAck?.let {
            packetsSinceLastAck = 0
            packetWriter.sendOrQueuePacket(it)
        }
    }

    /**
     * Process received packet(s) in the queue
     */
    private suspend fun processDataQueue() {
        delayedNACKJob?.cancel()
        while (sequenceInCursor in pendingPackets) {
            val packet = pendingPackets.remove(sequenceInCursor)!!
            ack(packet.sequence)
            pebblePacketAssembler.assemble(packet.data).collect {
                rxPebblePacketChannel.send(it)
            }
            sequenceInCursor = incrementSequence(sequenceInCursor)
        }
        if (pendingPackets.isNotEmpty()) {
            // We have out of order packets, schedule a resend of last ACK
            scheduleDelayedNACK()
        }
    }

    private suspend fun scheduleDelayedNACK() {
        delayedNACKJob?.cancel()
        val job = scope.launch(start = CoroutineStart.LAZY) {
            delay(OUT_OF_ORDER_MAX_DELAY_MS)
            if (pendingPackets.isNotEmpty()) {
                pendingPackets.clear()
                sendAck()
            }
        }
        delayedNACKJob = job
        jobActor.send {
            job.start()
            try {
                job.join()
            } catch (e: CancellationException) {
                Timber.v("Delayed NACK job cancelled")
            }
        }
    }

    private fun resetState() {
        sequenceInCursor = 0
        sequenceOutCursor = 0
        packetWriter.close()
        writerJob?.cancel()
        packetWriter = makePacketWriter()
        delayedNACKJob?.cancel()
        delayedAckJob?.cancel()
    }

    private suspend fun requestReset() {
        stateManager.state = State.AwaitingResetAckRequested
        resetState()
        packetWriter.rescheduleTimeout(true)
        packetWriter.sendOrQueuePacket(GATTPacket(GATTPacket.PacketType.RESET, 0, byteArrayOf(ppogVersion.value)))
    }

    private fun onTimeout() {
        scope.launch {
            if (stateManager.state in listOf(State.AwaitingResetAck, State.AwaitingResetAckRequested)) {
                Timber.w("Timeout in state ${stateManager.state}, resetting")
                if (++failedResetAttempts > MAX_FAILED_RESETS) {
                    throw PPoGSessionException("Failed to reset connection after $MAX_FAILED_RESETS attempts")
                }
                requestReset()
            }
            //TODO: handle data timeout
        }
    }

    fun openPacketFlow() = rxPebblePacketChannel.consumeAsFlow()

    override fun close() {
        resetState()
    }
}