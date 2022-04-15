package io.rebble.cobble.bluetooth

import android.bluetooth.*
import android.content.Context
import io.rebble.cobble.bluetooth.gatt.GATTServerMessage
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.ble.GATTPacket
import io.rebble.libpebblecommon.ble.LEConstants
import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.lang.Math.min
import java.nio.ByteBuffer
import java.util.*

class PPoGATTServer(
        private val targetDevice: BluetoothDevice,
        private val context: Context,
        private val serverScope: CoroutineScope,
        private val protocolHandler: ProtocolHandler,
        bluetoothManager: BluetoothManager
): GATTServer(bluetoothManager, context, serverScope) {
    private val gattDeviceCharUUID = UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER)
    private val gattMetaCharUUID = UUID.fromString(LEConstants.UUIDs.META_CHARACTERISTIC_SERVER)

    private var seq = 0
    private var remoteSeq = 0
    private var currentRXPend = 0
    private var maxPayload = 25

    private var lastAck: GATTPacket? = null

    private var connectionVersion = GATTPacket.PPoGConnectionVersion.ZERO
    private var maxRXWindow = LEConstants.MAX_RX_WINDOW
    private var maxTXWindow = LEConstants.MAX_TX_WINDOW
    private val pendingPackets: MutableList<GATTPacket> = mutableListOf()
    private val ackPending: MutableMap<Int, CompletableDeferred<GATTPacket>> = mutableMapOf()
    private var delayedAckJob: Job? = null

    private var receiveBuf: MutableList<Byte> = mutableListOf()
    private var pendingLength = 0
    private var initialReset = false

    private lateinit var dataCharacteristic: BluetoothGattCharacteristic
    private var rxJob: Job? = null

    val connectionStateFlow = serverMessages
            .filterIsInstance<GATTServerMessage.ConnectionStateChange>()
            .filter { it.device == targetDevice }

    sealed class SendActorMessage {
        object SendReset : SendActorMessage()
        object SendResetAck : SendActorMessage()
        data class SendAck(val sequence: Int) : SendActorMessage()
        object UpdateData : SendActorMessage()
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private val sendActor = serverScope.actor<SendActorMessage>(capacity = Channel.UNLIMITED) {
        for (message in this) {
            when (message) {
                is SendActorMessage.SendAck -> {
                    Timber.d("<- ACK ${message.sequence}")
                    if (!connectionVersion.supportsCoalescedAcking) {
                        currentRXPend = 0
                        writePacket(GATTPacket.PacketType.ACK, null, message.sequence)
                    }else {
                        currentRXPend++
                        delayedAckJob?.cancel()
                        if (currentRXPend >= maxRXWindow) {
                            currentRXPend = 0
                            writePacket(GATTPacket.PacketType.ACK, null, message.sequence)
                        }else {
                            delayedAckJob = serverScope.launch {
                                try {
                                    delay(500)
                                    writePacket(GATTPacket.PacketType.ACK, null, message.sequence)
                                } catch (e: CancellationException) {}
                            }
                        }
                    }
                }

                SendActorMessage.SendReset -> {
                    Timber.d("<- RESET")
                    writePacket(GATTPacket.PacketType.RESET, byteArrayOf(connectionVersion.value), 0)
                }

                SendActorMessage.SendResetAck -> {
                    Timber.d("<- RESETACK")
                    val data = if (connectionVersion.supportsWindowNegotiation) byteArrayOf(maxRXWindow, maxTXWindow) else null
                    writePacket(GATTPacket.PacketType.RESET_ACK, data, 0)
                    reset()
                }

                SendActorMessage.UpdateData -> {
                    if (pendingPackets.isNotEmpty()) {
                        val packet = pendingPackets.removeFirst()
                        Timber.d("<- DATA ${packet.sequence}")
                        notifyValue(targetDevice, packet.toByteArray(), gattDeviceCharUUID)
                    }
                }
            }
        }
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private val receiveActor = serverScope.actor<GATTPacket>(capacity = Channel.UNLIMITED) {
        for (packet in this) {
            when (packet.type) {
                GATTPacket.PacketType.DATA -> {
                    Timber.d("-> DATA ${packet.sequence}")
                    if (packet.sequence == remoteSeq) {
                        sendAck(packet.sequence)
                        remoteSeq = getNextSeq(remoteSeq)
                        val data = packet.toByteArray().drop(1).toByteArray()
                        if (receiveBuf.isEmpty()) {
                            pendingLength = getPacketLength(data).toInt()
                        }
                        receiveBuf.addAll(data.asList())

                        while (receiveBuf.size >= pendingLength+4) {
                            val range = 0..(pendingLength+3)
                            protocolHandler.receivePacket(receiveBuf.slice(range).toByteArray().asUByteArray())
                            receiveBuf = receiveBuf.subList(range.last, receiveBuf.size)
                        }
                    }else {
                        Timber.w("Unexpected sequence ${packet.sequence}, expected $remoteSeq")
                        lastAck?.let {
                            Timber.i("Sending clarifying ACK (${it.sequence})")
                            writePacket(GATTPacket.PacketType.ACK, null, it.sequence)
                        }
                    }
                }

                GATTPacket.PacketType.ACK -> {
                    Timber.d("-> ACK ${packet.sequence}")
                    for (i in 0..packet.sequence) {
                        ackPending.remove(i)?.let {
                            it.complete(packet)
                            updateData()
                        }
                    }
                }

                GATTPacket.PacketType.RESET -> {
                    Timber.d("-> RESET ${packet.sequence}")
                    if (seq != 0) {
                        Timber.e("Got reset on non zero sequence")
                    }
                    connectionVersion = packet.getPPoGConnectionVersion()
                    sendResetAck()
                }

                GATTPacket.PacketType.RESET_ACK -> {
                    Timber.d("-> RESETACK ${packet.sequence}")
                    Timber.d("Connection version  ${connectionVersion.value}")
                    if (connectionVersion.supportsWindowNegotiation && !packet.hasWindowSizes()) {
                        Timber.w("FW does not support window sizes in reset complete, reverting to connectionVersion 0")
                        connectionVersion = GATTPacket.PPoGConnectionVersion.ZERO
                    }

                    if (connectionVersion.supportsWindowNegotiation) {
                        maxRXWindow = packet.getMaxRXWindow().coerceAtMost(LEConstants.MAX_RX_WINDOW)
                        maxTXWindow = packet.getMaxTXWindow().coerceAtMost(LEConstants.MAX_TX_WINDOW)
                        Timber.i("GATTService: Windows negotiated: rx = $maxRXWindow, tx = $maxTXWindow")
                    }
                    sendResetAck()
                    if (!initialReset) {
                        Timber.i("Initial reset, everything is connected now")
                    }
                    initialReset = true
                }
            }
        }
    }

    private val gattPacketsRX = serverMessages
            .filterIsInstance<GATTServerMessage.CharacteristicWriteRequest>()
            .filter {
                it.device == targetDevice && it.characteristic?.uuid == gattDeviceCharUUID
            }
            .map { it.value?.let { payload -> GATTPacket(payload) } }
            .filterNotNull()

    private fun getPacketLength(packet: ByteArray): UShort {
        val headBuf = ByteBuffer.wrap(packet)
        return headBuf.short.toUShort()
    }

    private fun getNextSeq(current: Int): Int {
        return (current + 1) % 32
    }

    private suspend fun sendAck(sequence: Int) {
        require(sequence in 0..31)
        sendActor.send(SendActorMessage.SendAck(sequence))
    }

    private suspend fun sendResetAck() {
        sendActor.send(SendActorMessage.SendResetAck)
    }

    private suspend fun updateData() {
        sendActor.send(SendActorMessage.UpdateData)
    }

    private fun reset() {
        Timber.i("Resetting")
        remoteSeq = 0
        seq = 0
        lastAck = null
        pendingPackets.clear()
        ackPending.forEach { it.value.cancel() }
        ackPending.clear()
    }

    private suspend fun writePacket(type: GATTPacket.PacketType, data: ByteArray?, sequence: Int? = null) {
        val packet = GATTPacket(type, sequence ?: seq, if ((data?.size ?: 0) > 0) data else null)
        when (type) {
            GATTPacket.PacketType.DATA -> {
                val ackPendingCompletable = CompletableDeferred<GATTPacket>()
                ackPending[packet.sequence] = ackPendingCompletable

                if (ackPending.size >= maxTXWindow.toInt()) {
                    withTimeout(5000) {
                        val res = ackPendingCompletable.await()
                    }
                }

                pendingPackets.add(packet)
                if (sequence == null) {
                    this.seq = getNextSeq(this.seq)
                }

                updateData()
            }

            GATTPacket.PacketType.ACK -> {
                lastAck = packet
                notifyValue(targetDevice, packet.toByteArray(), gattDeviceCharUUID)
            }

            else -> {
                notifyValue(targetDevice, packet.toByteArray(), gattDeviceCharUUID)
            }
        }
    }

    fun write(rawProtocolPacket: ByteArray) {
        val maxPacketSize = maxPayload-1
        val chunked = rawProtocolPacket.asList().chunked(maxPacketSize).toMutableList()
        Timber.d("Writing packet of length ${rawProtocolPacket.size} in ${chunked.size} chunks")
        serverScope.launch(Dispatchers.IO) {
            while (chunked.isNotEmpty()) {
                writePacket(GATTPacket.PacketType.DATA, chunked.removeFirst().toByteArray())
            }
        }
    }

    private suspend fun setupMeta() {
        serverScope.launch {
            serverMessages
                    .filterIsInstance<GATTServerMessage.CharacteristicReadRequest>()
                    .filter {
                        it.device == targetDevice && it.characteristic?.uuid == gattMetaCharUUID
                    }
                    .collect {
                        Timber.d("Meta characteristic read")
                        sendResponse(it.device!!, it.requestId, 0, 0, LEConstants.SERVER_META_RESPONSE)
                    }
        }
    }

    suspend fun initServer(): Boolean {
        Timber.d("initServer()")
        setupMeta()
        rxJob = serverScope.launch(Dispatchers.IO) {
            gattPacketsRX.collect {
                receiveActor.send(it)
            }
        }
        val gattService = BluetoothGattService(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER), BluetoothGattService.SERVICE_TYPE_PRIMARY)
        gattService.addCharacteristic(BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.META_CHARACTERISTIC_SERVER), BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED))
        dataCharacteristic = BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER), BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE or BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED)
        dataCharacteristic.addDescriptor(BluetoothGattDescriptor(UUID.fromString(LEConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR), BluetoothGattDescriptor.PERMISSION_WRITE))
        gattService.addCharacteristic(dataCharacteristic)

        val padService = BluetoothGattService(UUID.fromString(LEConstants.UUIDs.FAKE_SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY)
        padService.addCharacteristic(BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.FAKE_SERVICE_UUID), BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ))

        /*if (bluetoothGattServer.getService(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER)) != null) {
            Timber.w("Service already registered, clearing services and then re-registering")
            this.bluetoothGattServer.clearServices()
        }*/
        return if (addService(gattService) && addService(padService)) {
            Timber.d("Server set up and ready for connection")
            true
        } else {
            Timber.e("Failed to add service")
            false
        }
    }

    fun setMTU(newMTU: Int) {
        maxPayload = newMTU
    }

    fun onNewPacketToSend(packet: ProtocolHandler.PendingPacket) {
        write(packet.data.asByteArray())
    }

    fun closePebble() {
        sendActor.close()
        receiveActor.close()
        rxJob?.cancel()
    }
}