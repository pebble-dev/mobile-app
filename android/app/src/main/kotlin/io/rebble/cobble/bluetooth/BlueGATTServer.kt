package io.rebble.cobble.bluetooth

import android.bluetooth.*
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import timber.log.Timber
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.ByteBuffer
import kotlin.experimental.and

class BlueGATTServer(private val targetDevice: BluetoothDevice, private val context: Context, private val serverScope: CoroutineScope) : BluetoothGattServerCallback() {
    private val serverReady = CompletableDeferred<Boolean>()
    private val connectionStatusChannel = Channel<Boolean>(0)

    private val ackPending: MutableMap<Int, CompletableDeferred<GATTPacket>> = mutableMapOf()

    private var mtu = BlueGATTConstants.DEFAULT_MTU
    private var seq: Int = 0
    private var remoteSeq: Int = 0
    private var lastAck: GATTPacket? = null
    private var packetsInFlight = 0
    private var gattConnectionVersion = GATTPacket.PPoGConnectionVersion.ZERO
    private var maxRxWindow: Byte = BlueGATTConstants.MAX_RX_WINDOW
    private var currentRxPend = 0
    private var maxTxWindow: Byte = BlueGATTConstants.MAX_TX_WINDOW
    private var delayedAckJob: Job? = null

    private lateinit var bluetoothGattServer: BluetoothGattServer
    private lateinit var dataCharacteristic: BluetoothGattCharacteristic

    val inputStream = PipedInputStream()
    val packetOutputStream = PipedOutputStream(inputStream)

    private val packetWriteInputStream = PipedInputStream()
    val outputStream = PipedOutputStream(packetWriteInputStream)

    var connected = false
    private var initialReset = false

    sealed class SendActorMessage {
        object SendReset: SendActorMessage()
        object SendResetAck: SendActorMessage()
        data class SendAck(val sequence: Int): SendActorMessage()
        data class ForceSendAck(val sequence: Int): SendActorMessage()
        object UpdateData: SendActorMessage()
    }
    private val sendActor = serverScope.actor<SendActorMessage> {
        for (message in this) {
            when (message) {
                is SendActorMessage.SendReset -> {
                    attemptWrite(GATTPacket(GATTPacket.PacketType.RESET, 0, byteArrayOf(gattConnectionVersion.value)))
                    reset()
                }
                is SendActorMessage.SendAck -> {
                    val ack = GATTPacket(GATTPacket.PacketType.ACK, message.sequence)

                    if (!gattConnectionVersion.supportsCoalescedAcking) {
                        currentRxPend = 0
                        attemptWrite(ack)
                        lastAck = ack
                    }else {
                        currentRxPend++
                        delayedAckJob?.cancel()
                        if (currentRxPend >= maxRxWindow / 2) {
                            currentRxPend = 0
                            attemptWrite(ack)
                            lastAck = ack
                        }else {
                            delayedAckJob = serverScope.launch {
                                delay(200)
                                this@actor.channel.offer(SendActorMessage.ForceSendAck(message.sequence))
                            }
                        }
                    }
                }
                is SendActorMessage.SendResetAck -> {
                    attemptWrite(GATTPacket(GATTPacket.PacketType.RESET_ACK, 0, if (gattConnectionVersion.supportsWindowNegotiation) byteArrayOf(maxRxWindow, maxTxWindow) else null))
                }
                is SendActorMessage.UpdateData -> {
                    if (packetsInFlight < maxTxWindow) {
                        if (packetWriteInputStream.available() > 0) {
                            val count = packetWriteInputStream.available().coerceAtMost(mtu - 4)
                            val buf = ByteBuffer.allocate(count)
                            packetWriteInputStream.readFully(buf, 0, count)
                            attemptWrite(GATTPacket(GATTPacket.PacketType.DATA, seq, buf.array()))
                            seq = getNextSeq(seq)
                        }
                    }
                }
                is SendActorMessage.ForceSendAck -> {
                    val ack = GATTPacket(GATTPacket.PacketType.ACK, message.sequence)
                    currentRxPend = 0
                    attemptWrite(ack)
                    lastAck = ack
                }
            }
        }
    }

    fun onNewPacketToSend() {
        sendActor.offer(SendActorMessage.UpdateData)
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        val gattStatus = GattStatus(status)
        when (service?.uuid) {
            BlueGATTConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER -> {
                if (gattStatus.isSuccess()) {
                    // No idea why this is needed, but stock app does this
                    val padService = BluetoothGattService(BlueGATTConstants.UUIDs.FAKE_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
                    padService.addCharacteristic(BluetoothGattCharacteristic(BlueGATTConstants.UUIDs.FAKE_SERVICE_UUID, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ))
                    bluetoothGattServer.addService(padService)
                } else {
                    Timber.e("Failed to add service! Status: ${gattStatus}")
                    serverReady.complete(false)
                }
            }

            BlueGATTConstants.UUIDs.FAKE_SERVICE_UUID -> {
                // Server is init'd
                serverReady.complete(true)
            }
        }
    }

    override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
        if (targetDevice.address == device!!.address) {
            if (value != null) {
                serverScope.launch(Dispatchers.IO) {
                    val packet = GATTPacket(value)
                    when (packet.type) {
                        GATTPacket.PacketType.RESET_ACK -> {
                            Timber.d("Got reset ACK")
                            if (gattConnectionVersion.supportsWindowNegotiation && !packet.hasWindowSizes()) {
                                Timber.d("FW does not support window sizes in reset complete, reverting to gattConnectionVersion 0")
                                gattConnectionVersion = GATTPacket.PPoGConnectionVersion.ZERO
                            }
                            if (gattConnectionVersion.supportsWindowNegotiation) {
                                maxRxWindow = packet.getMaxRXWindow().coerceAtMost(BlueGATTConstants.MAX_RX_WINDOW)
                                maxTxWindow = packet.getMaxTXWindow().coerceAtMost(BlueGATTConstants.MAX_TX_WINDOW)
                                Timber.d("Windows negotiated: maxRxWindow = $maxRxWindow, maxTxWindow = $maxTxWindow")
                            }
                            sendResetAck(packet.sequence)
                        }
                        GATTPacket.PacketType.ACK -> {
                            for (i in 0..packet.sequence) {
                                ackPending.remove(i)?.complete(packet)
                                packetsInFlight = (packetsInFlight-1).coerceAtLeast(0)
                            }
                            Timber.d("Got ACK for ${packet.sequence}")
                        }
                        GATTPacket.PacketType.DATA -> {
                            Timber.d("Packet ${packet.sequence}, Expected $remoteSeq")
                            if (packet.sequence == remoteSeq) {
                                try {
                                    remoteSeq = getNextSeq(remoteSeq)
                                    packetOutputStream.write(packet.data.copyOfRange(1, packet.data.size))
                                    packetOutputStream.flush()
                                    sendAck(packet.sequence)
                                } catch (e: IOException) {
                                    Timber.e(e, "Error writing to packetOutputStream")
                                    closePebble()
                                    return@launch
                                }
                            }else {
                                Timber.w("Unexpected sequence ${packet.sequence}")
                                if (lastAck != null && lastAck!!.type != GATTPacket.PacketType.RESET_ACK) {
                                    Timber.d("Re-sending previous ACK")
                                    sendAck(lastAck!!.sequence)
                                }else {
                                    requestReset()
                                }
                            }
                        }
                        GATTPacket.PacketType.RESET -> {
                            if (seq != 0) Timber.w("Got reset on non zero sequence")
                            gattConnectionVersion = packet.getPPoGConnectionVersion()
                            Timber.d("gattConnectionVersion updated: $gattConnectionVersion")
                            reset()
                            sendResetAck(packet.sequence)
                        }
                    }
                }
            } else {
                Timber.w("Data was null, ignoring")
            }
        } else {
            Timber.w("Device was not target device, ignoring")
        }
    }

    override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
        if (targetDevice.address == device!!.address) {
            if (characteristic?.uuid == BlueGATTConstants.UUIDs.META_CHARACTERISTIC_SERVER) {
                Timber.d("Meta queried")
                connected = true
                if (!bluetoothGattServer.sendResponse(device, requestId, 0, offset, BlueGATTConstants.SERVER_META_RESPONSE)) {
                    Timber.e("Error sending meta response to device")
                    closePebble()
                }else {
                    serverScope.launch {
                        delay(5000)
                        if (!initialReset) {
                            Timber.w("No initial reset from watch after 5s, requesting reset")
                            requestReset()
                        }
                    }
                }
            }
        } else {
            Timber.w("Device was not target device, ignoring")
        }
    }

    override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
        if (targetDevice.address == device!!.address) {
            if (descriptor?.characteristic?.uuid == BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER) {
                if (value != null) {
                    serverScope.launch(Dispatchers.IO) {
                        if (!bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value)) {
                            Timber.e("Failed to send confirm for descriptor write")
                            closePebble()
                        }
                        if ((value[0] and 1) == 0.toByte()) { // if notifications disabled
                            Timber.d("Device requested disable notifications")
                            closePebble()
                        }
                    }
                } else {
                    Timber.w("Data was null, ignoring")
                }
            }
        } else {
            Timber.w("Device was not target device, ignoring")
        }
    }

    override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
        if (targetDevice.address == device!!.address) {
            Timber.d("onNotificationSent")
            sendActor.offer(SendActorMessage.UpdateData)
        }
    }

    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
        if (targetDevice.address == device!!.address) {
            val gattStatus = GattStatus(status)
            if (gattStatus.isSuccess()) {
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        Timber.d("Device connected")
                    }

                    BluetoothGatt.STATE_DISCONNECTED -> {
                        if (targetDevice.address == device.address && initialReset) {
                            connected = false
                            serverScope.launch {
                                delay(1000)
                                if (!connected) {
                                    Timber.d("Device disconnected, closing")
                                    closePebble()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Create the server and add its characteristics for the watch to use
     */
    suspend fun initServer(): Boolean {
        val bluetoothManager = context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothGattServer = bluetoothManager.openGattServer(context, this)!!

        val gattService = BluetoothGattService(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        gattService.addCharacteristic(BluetoothGattCharacteristic(BlueGATTConstants.UUIDs.META_CHARACTERISTIC_SERVER, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED))
        dataCharacteristic = BluetoothGattCharacteristic(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE or BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED)
        dataCharacteristic.addDescriptor(BluetoothGattDescriptor(BlueGATTConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR, BluetoothGattDescriptor.PERMISSION_WRITE))
        gattService.addCharacteristic(dataCharacteristic)
        if (bluetoothGattServer.getService(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER) != null) {
            Timber.w("Service already registered, clearing services and then re-registering")
            this.bluetoothGattServer.clearServices()
        }
        if (bluetoothGattServer.addService(gattService) && serverReady.await()) {
            Timber.d("Server set up and ready for connection")
        } else {
            Timber.e("Failed to add service")
            return false
        }
        return true
    }

    /**
     * Returns the next sequence that will be used
     */
    private fun getNextSeq(current: Int): Int {
        return (current + 1) % 32
    }

    /**
     * Update the MTU for the server to check packet sizes against
     */
    fun setMTU(newMTU: Int) {
        this.mtu = newMTU
    }

    /**
     * attempt to write to data characteristic, error conditions being no ACK received or failing to get the write lock
     */
    private suspend fun attemptWrite(packet: GATTPacket) {
        withContext(Dispatchers.IO) {
            Timber.d("Sending ${packet.type}: ${packet.sequence}")
            if (packet.type == GATTPacket.PacketType.DATA || packet.type == GATTPacket.PacketType.RESET) ackPending[packet.sequence] = CompletableDeferred(packet)
            var success = false
            var attempt = 0
            if (packet.type == GATTPacket.PacketType.DATA) packetsInFlight++
            while (!success && attempt < 3) {
                dataCharacteristic.value = packet.data
                if (!bluetoothGattServer.notifyCharacteristicChanged(targetDevice, dataCharacteristic, false)) {
                    Timber.w("notifyCharacteristicChanged failed")
                    attempt++
                    continue
                }

                if (packet.type == GATTPacket.PacketType.DATA || packet.type == GATTPacket.PacketType.RESET) {
                    try {
                        withTimeout(5000) {
                            ackPending[packet.sequence]?.await()
                            success = true
                        }
                    } catch (e: CancellationException) {
                        Timber.w("ACK wait timed out")
                        attempt++
                    }
                } else {
                    success = true
                }
            }
            if (!success) {
                Timber.e("Gave up sending packet")
            }
        }
    }

    /**
     * Send reset packet to watch (usually should never need to happen) that resets sequence and pending pebble packet buffer
     */
    private suspend fun requestReset() {
        Timber.w("Requesting reset")
        sendActor.offer(SendActorMessage.SendReset)
        reset()
    }

    /**
     * Phone side reset, clears buffers, pending packets and resets sequence back to 0
     */
    private suspend fun reset() {
        Timber.d("Resetting LE")
        packetWriteInputStream.skip(packetWriteInputStream.available().toLong())
        ackPending.forEach{
            it.value.cancel()
        }
        ackPending.clear()
        remoteSeq = 0
        seq = 0
        lastAck = null
        packetsInFlight = 0
        if(!initialReset) {
            connectionStatusChannel.send(true)
        }
        initialReset = true
        sendActor.offer(SendActorMessage.UpdateData)
    }

    /**
     * Send an ACK for a packet
     */
    private fun sendAck(sequence: Int) {
        Timber.d("Sending ACK for $sequence")
        sendActor.offer(SendActorMessage.SendAck(sequence))
    }

    /**
     * Send a reset ACK
     */
    private fun sendResetAck(sequence: Int) {
        Timber.d("Sending reset ACK for $sequence")
        sendActor.offer(SendActorMessage.SendResetAck)
    }

    /**
     * Simply suspends the caller until a connection succeeded or failed, AKA its connected or not
     */
    suspend fun connectPebble(): Boolean {
        return connectionStatusChannel.receive()
    }

    fun closePebble() {
        Timber.d("Server closing connection")
        sendActor.close()
        connectionStatusChannel.offer(false)
        packetOutputStream.close()
        inputStream.close()
        bluetoothGattServer.cancelConnection(targetDevice)
        bluetoothGattServer.clearServices()
        bluetoothGattServer.close()
    }

}