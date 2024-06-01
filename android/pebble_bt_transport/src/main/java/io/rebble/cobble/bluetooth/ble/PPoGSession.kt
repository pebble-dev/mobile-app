package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothDevice
import io.rebble.cobble.bluetooth.ble.util.chunked
import io.rebble.libpebblecommon.ble.GATTPacket
import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.Closeable
import java.util.LinkedList
import kotlin.math.min

class PPoGSession(private val scope: CoroutineScope, private val deviceAddress: String, var mtu: Int): Closeable {
    class PPoGSessionException(message: String) : Exception(message)

    private val pendingPackets = mutableMapOf<Int, GATTPacket>()
    private var ppogVersion: GATTPacket.PPoGConnectionVersion = GATTPacket.PPoGConnectionVersion.ZERO

    private var rxWindow = 0
    private var packetsSinceLastAck = 0
    private var sequenceInCursor = 0
    private var sequenceOutCursor = 0
    private var lastAck: GATTPacket? = null
    private val delayedAckScope = scope + Job()
    private var delayedNACKScope = scope + Job()
    private var resetAckJob: Job? = null
    private var writerJob: Job? = null
    private var failedResetAttempts = 0
    private val pebblePacketAssembler = PPoGPebblePacketAssembler()

    private val sessionFlow = MutableSharedFlow<PPoGSessionResponse>()
    private val packetRetries: MutableMap<GATTPacket, Int> = mutableMapOf()
    private var pendingOutboundResetAck: GATTPacket? = null

    open class PPoGSessionResponse {
        class PebblePacket(val packet: ByteArray) : PPoGSessionResponse()
        class WritePPoGCharacteristic(val data: ByteArray, val result: CompletableDeferred<Boolean>) : PPoGSessionResponse()
    }
    open class SessionCommand {
        class SendMessage(val data: ByteArray) : SessionCommand()
        class HandlePacket(val packet: ByteArray) : SessionCommand()
        class SetMTU(val mtu: Int) : SessionCommand()
        class SendPendingResetAck : SessionCommand()
        class OnUnblocked : SessionCommand() //TODO
        class DelayedAck : SessionCommand()
        class DelayedNack : SessionCommand()
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private val sessionActor = scope.actor<SessionCommand>(capacity = 8) {
        for (command in channel) {
            when (command) {
                is SessionCommand.SendMessage -> {
                    if (stateManager.state != State.Open) {
                        throw PPoGSessionException("Session not open")
                    }
                    val dataChunks = command.data.chunked(stateManager.mtuSize - 3)
                    for (chunk in dataChunks) {
                        val packet = GATTPacket(GATTPacket.PacketType.DATA, sequenceOutCursor, chunk)
                        packetWriter.sendOrQueuePacket(packet)
                        sequenceOutCursor = incrementSequence(sequenceOutCursor)
                    }
                }
                is SessionCommand.HandlePacket -> {
                    val ppogPacket = GATTPacket(command.packet)
                    try {
                        withTimeout(1000L) {
                            when (ppogPacket.type) {
                                GATTPacket.PacketType.RESET -> onResetRequest(ppogPacket)
                                GATTPacket.PacketType.RESET_ACK -> onResetAck(ppogPacket)
                                GATTPacket.PacketType.ACK -> onAck(ppogPacket)
                                GATTPacket.PacketType.DATA -> {
                                    Timber.v("-> DATA ${ppogPacket.sequence}")
                                    pendingPackets[ppogPacket.sequence] = ppogPacket
                                    processDataQueue()
                                }
                            }
                        }
                    } catch (e: TimeoutCancellationException) {
                        Timber.e("Timeout while processing packet ${ppogPacket.type} ${ppogPacket.sequence}")
                    }
                }
                is SessionCommand.SetMTU -> {
                    mtu = command.mtu
                }
                is SessionCommand.SendPendingResetAck -> {
                    pendingOutboundResetAck?.let {
                        Timber.i("Connection is now allowed, sending pending reset ACK")
                        packetWriter.sendOrQueuePacket(it)
                        pendingOutboundResetAck = null
                    }
                }
                is SessionCommand.OnUnblocked -> {
                    packetWriter.sendNextPacket()
                }
                is SessionCommand.DelayedAck -> {
                    delayedAckScope.coroutineContext.job.cancelChildren()
                    delayedAckScope.launch {
                        delay(COALESCED_ACK_DELAY_MS)
                        sendAck()
                    }.join()
                }
                is SessionCommand.DelayedNack -> {
                    delayedNACKScope.coroutineContext.job.cancelChildren()
                    delayedNACKScope.launch {
                        delay(OUT_OF_ORDER_MAX_DELAY_MS)
                        sendAck()
                    }.join()
                }
            }
        }
    }

    fun sendMessage(data: ByteArray): Boolean = sessionActor.trySend(SessionCommand.SendMessage(data)).isSuccess
    fun handlePacket(packet: ByteArray): Boolean = sessionActor.trySend(SessionCommand.HandlePacket(packet)).isSuccess
    fun setMTU(mtu: Int): Boolean = sessionActor.trySend(SessionCommand.SetMTU(mtu)).isSuccess
    fun onUnblocked(): Boolean = sessionActor.trySend(SessionCommand.OnUnblocked()).isSuccess

    inner class StateManager {
        private var _state = State.Closed
        var state: State
            get() = _state
            set(value) {
                Timber.d("State changed from ${_state.name} to ${value.name}")
                _state = value
            }
        var mtuSize: Int get() = mtu
            set(_) {}
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
        private const val MAX_NUM_RETRIES = 2
    }

    enum class State(val allowedRxTypes: List<GATTPacket.PacketType>, val allowedTxTypes: List<GATTPacket.PacketType>) {
        Closed(listOf(GATTPacket.PacketType.RESET), listOf(GATTPacket.PacketType.RESET_ACK)),
        AwaitingResetAck(listOf(GATTPacket.PacketType.RESET_ACK), listOf(GATTPacket.PacketType.RESET, GATTPacket.PacketType.RESET_ACK)),
        AwaitingResetAckRequested(listOf(GATTPacket.PacketType.RESET_ACK), listOf(GATTPacket.PacketType.RESET, GATTPacket.PacketType.RESET_ACK)),
        Open(listOf(GATTPacket.PacketType.RESET, GATTPacket.PacketType.ACK, GATTPacket.PacketType.DATA), listOf(GATTPacket.PacketType.ACK, GATTPacket.PacketType.DATA)),
    }

    private fun makePacketWriter(): PPoGPacketWriter {
        val writer = PPoGPacketWriter(scope, stateManager) { onTimeout() }
        writerJob = writer.packetWriteFlow.onEach {
            Timber.v("<- ${it.type.name} ${it.sequence}")
            val resultCompletable = CompletableDeferred<Boolean>()
            sessionFlow.emit(PPoGSessionResponse.WritePPoGCharacteristic(it.toByteArray(), resultCompletable))
            packetWriter.setPacketSendStatus(it, resultCompletable.await())
        }.launchIn(scope)
        return writer
    }

    private suspend fun onResetRequest(packet: GATTPacket) {
        require(packet.type == GATTPacket.PacketType.RESET)
        Timber.v("-> RESET ${packet.sequence}")
        if (packet.sequence != 0) {
            throw PPoGSessionException("Reset packet must have sequence 0")
        }
        val nwVersion = packet.getPPoGConnectionVersion()
        Timber.d("Reset requested, new PPoGATT version: $nwVersion")
        ppogVersion = nwVersion
        packetWriter.rescheduleTimeout(true)
        resetState()
        val resetAckPacket = makeResetAck(sequenceOutCursor, MAX_SUPPORTED_WINDOW_SIZE, MAX_SUPPORTED_WINDOW_SIZE, ppogVersion)
        stateManager.state = State.AwaitingResetAck
        if (PPoGLinkStateManager.getState(deviceAddress).value != PPoGLinkState.ReadyForSession) {
            Timber.i("Connection not allowed yet, saving reset ACK for later")
            pendingOutboundResetAck = resetAckPacket
            scope.launch {
                PPoGLinkStateManager.getState(deviceAddress).first { it == PPoGLinkState.ReadyForSession }
                sessionActor.send(SessionCommand.SendPendingResetAck())
            }
            return
        }
        packetWriter.sendOrQueuePacket(resetAckPacket)
    }

    private fun makeResetAck(sequence: Int, rxWindow: Int, txWindow: Int, ppogVersion: GATTPacket.PPoGConnectionVersion): GATTPacket {
        return GATTPacket(GATTPacket.PacketType.RESET_ACK, sequence, if (ppogVersion.supportsWindowNegotiation) {
            byteArrayOf(rxWindow.toByte(), txWindow.toByte())
        } else {
            null
        })
    }

    private suspend fun onResetAck(packet: GATTPacket) {
        require(packet.type == GATTPacket.PacketType.RESET_ACK)
        Timber.v("-> RESET_ACK ${packet.sequence}")
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
        PPoGLinkStateManager.updateState(deviceAddress, PPoGLinkState.SessionOpen)
    }

    private suspend fun onAck(packet: GATTPacket) {
        require(packet.type == GATTPacket.PacketType.ACK)
        Timber.v("-> ACK ${packet.sequence}")
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

    private fun scheduleDelayedAck() = sessionActor.trySend(SessionCommand.DelayedAck()).isSuccess
    private fun scheduleDelayedNACK() = sessionActor.trySend(SessionCommand.DelayedNack()).isSuccess

    /**
     * Send an ACK cancelling the delayed ACK job if present
     */
    private suspend fun sendAckCancelling() {
        delayedAckScope.coroutineContext.job.cancelChildren()
        sendAck()
    }


    var dbgLastAckSeq = -1

    /**
     * Send the last ACK packet
     */
    private suspend fun sendAck() {
        // Send ack
        lastAck?.let {
            packetsSinceLastAck = 0
            check(it.sequence != dbgLastAckSeq) { "Sending duplicate ACK for sequence ${it.sequence}" } //TODO: Check this issue
            dbgLastAckSeq = it.sequence
            Timber.d("Writing ACK for sequence ${it.sequence}")
            packetWriter.sendOrQueuePacket(it)
        }
    }

    /**
     * Process received packet(s) in the queue
     */
    private suspend fun processDataQueue() {
        delayedNACKScope.coroutineContext.job.cancelChildren()
        while (sequenceInCursor in pendingPackets) {
            val packet = pendingPackets.remove(sequenceInCursor)!!
            ack(packet.sequence)
            val pebblePacket = packet.data.sliceArray(1 until packet.data.size)
            pebblePacketAssembler.assemble(pebblePacket).collect {
                sessionFlow.emit(PPoGSessionResponse.PebblePacket(it))
            }
            sequenceInCursor = incrementSequence(sequenceInCursor)
        }
        if (pendingPackets.isNotEmpty()) {
            // We have out of order packets, schedule a resend of last ACK
            scheduleDelayedNACK()
        }
    }

    private fun resetState() {
        sequenceInCursor = 0
        sequenceOutCursor = 0
        packetWriter.close()
        writerJob?.cancel()
        packetWriter = makePacketWriter()
        delayedNACKScope.coroutineContext.job.cancelChildren()
        delayedAckScope.coroutineContext.job.cancelChildren()
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
            val packetsToResend = LinkedList<GATTPacket>()
            while (true) {
                val packet = packetWriter.inflightPackets.poll() ?: break
                if ((packetRetries[packet] ?: 0) <= MAX_NUM_RETRIES) {
                    Timber.w("Packet ${packet.sequence} timed out, resending")
                    packetsToResend.add(packet)
                    packetRetries[packet] = (packetRetries[packet] ?: 0) + 1
                } else {
                    Timber.w("Packet ${packet.sequence} timed out too many times, resetting")
                    requestReset()
                }
            }

            for (packet in packetsToResend.reversed()) {
                packetWriter.dataWaitingToSend.addFirst(packet)
            }
        }
    }
    fun flow() = sessionFlow.asSharedFlow()

    override fun close() {
        resetState()
    }
}