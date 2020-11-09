package io.rebble.fossil.bluetooth

import android.bluetooth.*
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.receiveOrNull
import timber.log.Timber
import kotlin.experimental.and

class BlueGATTServer(private val context: Context, private val gattPacketCallback: suspend (GATTPacket) -> Unit) : BlueGATTIO {
    private lateinit var dataReadCharacteristic: BluetoothGattCharacteristic
    private lateinit var dataWriteCharacteristic: BluetoothGattCharacteristic

    private val dataReadChannel = Channel<GATTPacket>(Channel.BUFFERED)
    private val packetWriteChannel = Channel<GATTPacket>(0)

    override var isConnected = false
    private val ackPending: MutableMap<UShort, CompletableDeferred<GATTPacket>> = mutableMapOf()
    private var sendPending: CompletableDeferred<Boolean>? = null
    private var targetDevice: BluetoothDevice? = null

    private var mtu = BlueGATTConstants.DEFAULT_MTU
    private var seq: UShort = 0U
    private var remoteSeq: UShort = 0U

    private lateinit var bluetoothGattServer: BluetoothGattServer
    private var dataCharacteristic: BluetoothGattCharacteristic

    private val gattServerCallbacks = object : BluetoothGattServerCallback() {
        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            val gattStatus = GattStatus(status)
            // No idea why this is needed, but stock app does this
            if (service?.uuid == BlueGATTConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER && gattStatus.isSuccess()) {
                val padService = BluetoothGattService(BlueGATTConstants.UUIDs.FAKE_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
                padService.addCharacteristic(BluetoothGattCharacteristic(BlueGATTConstants.UUIDs.FAKE_SERVICE_UUID, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ))
                bluetoothGattServer.addService(padService)
            } else if (service?.uuid == BlueGATTConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER) {
                Timber.e("Failed to add service! Status: ${gattStatus}")
            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            if (targetDevice?.address == device!!.address) {
                if (value != null) {
                    val packet = GATTPacket(value)
                    when (packet.type) {
                        GATTPacket.PacketType.RESET_ACK, GATTPacket.PacketType.ACK -> {
                            ackPending.remove(packet.sequence)?.complete(packet)
                            Timber.d("Got ACK for ${packet.sequence}")
                        }
                        GATTPacket.PacketType.DATA -> {
                            dataReadChannel.offer(packet)
                        }
                        GATTPacket.PacketType.RESET -> {
                            remoteSeq = 0U
                            GlobalScope.launch { sendAck(packet.sequence, true) } //XXX
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
            if (targetDevice?.address == device!!.address) {
                if (characteristic?.uuid == BlueGATTConstants.UUIDs.META_CHARACTERISTIC_SERVER) {
                    if (!bluetoothGattServer.sendResponse(device, requestId, 0, offset, BlueGATTConstants.SERVER_META_RESPONSE)) {
                        Timber.e("Error sending meta response to device")
                        //TODO: possibly disconnect?
                    }
                }
            } else {
                Timber.w("Device was not target device, ignoring")
            }
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            if (targetDevice?.address == device!!.address) {
                if (descriptor?.uuid == BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER) {
                    if (value != null) {
                        if ((value[0] and 1) > 0) { // if notifications enabled
                            //TODO: start sending packets
                            Timber.d("!! START SEND PACKETS !!")
                        } else {
                            Timber.d("Device requested disable notifications")
                            //TODO: disconnect
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
            if (targetDevice?.address == device!!.address) {
                sendPending?.complete(GattStatus(status).isSuccess())
            }
        }

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            val gattStatus = GattStatus(status)
            if (gattStatus.isSuccess()) {
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        if (targetDevice != null) {
                            Timber.w("New device attempted connection when targetDevice is already set, ignoring")
                        } else {
                            Timber.d("Device connected")
                            targetDevice = device
                        }
                    }

                    BluetoothGatt.STATE_DISCONNECTED -> {
                        if (targetDevice?.address == device!!.address) {
                            Timber.d("Device disconnected, resetting")
                            targetDevice = null
                            mtu = BlueGATTConstants.DEFAULT_MTU
                            seq = 0U
                            remoteSeq = 0U
                            sendPending?.cancel()
                            ackPending.clear()
                            isConnected = false
                            GlobalScope.launch {
                                while (!dataReadChannel.isEmpty) {
                                    dataReadChannel.receiveOrNull()
                                }
                                while (!packetWriteChannel.isEmpty) {
                                    packetWriteChannel.receiveOrNull()
                                }
                            }
                            //TODO: fully reset for next watch to connect cleanly
                        }
                    }
                }
            }
        }
    }

    init {
        val bluetoothManager = context.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallbacks)!!

        val gattService = BluetoothGattService(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        gattService.addCharacteristic(BluetoothGattCharacteristic(BlueGATTConstants.UUIDs.META_CHARACTERISTIC_SERVER, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED))
        dataCharacteristic = BluetoothGattCharacteristic(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE or BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED)
        dataCharacteristic.addDescriptor(BluetoothGattDescriptor(BlueGATTConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR, BluetoothGattDescriptor.PERMISSION_WRITE))
        gattService.addCharacteristic(dataCharacteristic)
        if (bluetoothGattServer.getService(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER) != null) {
            Timber.w("Service already registered, clearing services and then re-registering")
            this.bluetoothGattServer.clearServices()
        }
        if (bluetoothGattServer.addService(gattService)) {
            Timber.d("Server set up and ready for connection")
        } else {
            Timber.e("Failed to add service")
        }
        GlobalScope.launch { packetWriter() }
        GlobalScope.launch { packetReader() }
    }

    private fun getSeq(): UShort {
        if (seq == 32U.toUShort()) seq = 0U
        return seq++
    }

    private fun getExpectedRemoteSeq(): UShort {
        if (remoteSeq == 32U.toUShort()) remoteSeq = 0U
        return remoteSeq++
    }

    override fun setMTU(newMTU: Int) {
        this.mtu = newMTU
    }

    private suspend fun packetReader() = coroutineScope {
        launch(Dispatchers.IO) {
            while (true) {
                val packet = dataReadChannel.receive()
                val expected = getExpectedRemoteSeq()
                Timber.d("Packet ${packet.sequence}, Expected ${expected}")
                if (packet.sequence == expected) {
                    sendAck(packet.sequence)
                    gattPacketCallback(packet)
                }
            }
        }
    }

    private suspend fun packetWriter() = coroutineScope {
        launch(Dispatchers.IO) {
            while (true) {
                val packet = packetWriteChannel.receive()
                var success = false
                var tries = 0
                if (packet.type == GATTPacket.PacketType.DATA) Timber.d("Writing data packet ${packet.sequence}")
                if (packet.data.size >= mtu) {
                    Timber.e("Packet size too large for MTU")
                }
                while (++tries <= 3 && !success) {
                    dataWriteCharacteristic.value = packet.toByteArray()
                    if (!requestWritePacket()) {
                        Timber.e("Failed to write to data characteristic")
                        //TODO: retry/disconnect?
                    }
                    try {
                        withTimeout(1000) {
                            ackPending[packet.sequence]?.await()
                            success = true
                        }
                    } catch (e: TimeoutCancellationException) {
                        tries++
                    }
                }
                if (!success) {
                    Timber.e("Gave up sending packet, waiting for ACK timed out on all attempts")
                }
            }
        }
    }

    private suspend fun requestWritePacket(): Boolean {
        sendPending?.await()
        sendPending = CompletableDeferred()
        if (targetDevice != null) {
            return bluetoothGattServer.notifyCharacteristicChanged(targetDevice, dataCharacteristic, false)
        }
        return false
    }

    private suspend fun sendAck(sequence: UShort, reset: Boolean = false) {
        Timber.d("Sending ACK for ${sequence}")
        packetWriteChannel.send(GATTPacket(if (reset) GATTPacket.PacketType.RESET_ACK else GATTPacket.PacketType.ACK, sequence))
    }

    suspend fun sendBytesToDevice(bytes: ByteArray): Boolean {
        val mtu = this.mtu

        val splitBytes = BlueLEDriver.splitBytesByMTU(bytes, mtu)

        splitBytes.forEach {
            val thisSeq = getSeq()
            val result = CompletableDeferred<GATTPacket>()
            ackPending[thisSeq] = result
            packetWriteChannel.send(GATTPacket(GATTPacket.PacketType.DATA, thisSeq, it))
            try {
                withTimeout(3100) {
                    result.await()
                }
            } catch (e: TimeoutCancellationException) {
                return false
            }
        }
        return true
    }

    override fun requestReset() {
        val thisSeq = getSeq()
        GlobalScope.launch { packetWriteChannel.send(GATTPacket(GATTPacket.PacketType.RESET, thisSeq)) }
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        val gattStatus = GattStatus(status)
        if (!isConnected) return
        sendPending?.complete(true)
        if (characteristic?.uuid == dataWriteCharacteristic.uuid) {
            if (!gattStatus.isSuccess()) Timber.e("Data characteristic write failed: ${gattStatus}")
        }
    }


    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        if (characteristic?.uuid == BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_READ && characteristic != null) {
            val packet = GATTPacket(characteristic.value)
            when (packet.type) {
                GATTPacket.PacketType.RESET_ACK, GATTPacket.PacketType.ACK -> {
                    ackPending.remove(packet.sequence)?.complete(packet)
                    Timber.d("Got ACK for ${packet.sequence}")
                }
                GATTPacket.PacketType.DATA -> {
                    dataReadChannel.offer(packet)
                }
                GATTPacket.PacketType.RESET -> {
                    remoteSeq = 0U
                    GlobalScope.launch { sendAck(packet.sequence, true) } //XXX
                }
            }
        }
    }

    override fun sendPacket(bytes: ByteArray, callback: (Boolean) -> Unit) {
        GlobalScope.launch {
            callback(sendBytesToDevice(bytes))
        }
    }

    override fun connectPebble(): Boolean {
        return true
    }

}