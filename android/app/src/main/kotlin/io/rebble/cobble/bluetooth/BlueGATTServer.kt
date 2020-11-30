package io.rebble.cobble.bluetooth

import android.bluetooth.*
import android.content.Context
import io.rebble.libpebblecommon.protocolhelpers.ProtocolEndpoint
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import timber.log.Timber
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and

class BlueGATTServer(private val targetDevice: BluetoothDevice, private val context: Context) : BlueGATTIO {
    private val serverReady = CompletableDeferred<Boolean>()
    private val connectionStatusChannel = Channel<Boolean>(0)

    override var isConnected = false
    private val ackPending: MutableMap<UShort, CompletableDeferred<GATTPacket>> = mutableMapOf()
    private var sendPending: CompletableDeferred<Boolean>? = null

    private var mtu = BlueGATTConstants.DEFAULT_MTU
    private var seq: UShort = 0U
    private var remoteSeq: UShort = 0U

    private lateinit var bluetoothGattServer: BluetoothGattServer
    private lateinit var dataCharacteristic: BluetoothGattCharacteristic

    private var running = false

    override val inputStream = PipedInputStream()
    val packetOutputStream = PipedOutputStream(inputStream)

    private val packetWriteInputStream = PipedInputStream()
    val outputStream = PipedOutputStream(packetWriteInputStream)

    private val gattServerCallbacks = object : BluetoothGattServerCallback() {
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
            Timber.d("onCharacteristicWriteRequest: ${characteristic?.uuid}")
            if (targetDevice.address == device!!.address) {
                if (value != null) {
                    val packet = GATTPacket(value)
                    when (packet.type) {
                        GATTPacket.PacketType.RESET_ACK, GATTPacket.PacketType.ACK -> {
                            ackPending.remove(packet.sequence)?.complete(packet)
                            Timber.d("Got ACK for ${packet.sequence}")
                        }
                        GATTPacket.PacketType.DATA -> {
                            val expected = getExpectedRemoteSeq()
                            Timber.d("Packet ${packet.sequence}, Expected $expected")
                            if (packet.sequence == expected) {
                                try {
                                    packetOutputStream.write(packet.data.copyOfRange(1, packet.data.size))
                                    GlobalScope.launch(Dispatchers.IO) { sendAck(packet.sequence) }
                                } catch (e: IOException) {
                                    Timber.e(e, "Error writing to packetOutputStream")
                                }
                            }
                        }
                        GATTPacket.PacketType.RESET -> {
                            remoteSeq = 0U
                            GlobalScope.launch(Dispatchers.IO) { sendAck(packet.sequence, true) }
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
                Timber.d("onCharacteristicReadRequest: ${characteristic?.uuid}")
                if (characteristic?.uuid == BlueGATTConstants.UUIDs.META_CHARACTERISTIC_SERVER) {
                    Timber.d("Meta queried")
                    if (!bluetoothGattServer.sendResponse(device, requestId, 0, offset, BlueGATTConstants.SERVER_META_RESPONSE)) {
                        Timber.e("Error sending meta response to device")
                        closePebble()
                    }
                }
            } else {
                Timber.w("Device was not target device, ignoring")
            }
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            if (targetDevice.address == device!!.address) {
                Timber.d("onDescriptorWriteRequest: ${descriptor?.uuid}")
                if (descriptor?.characteristic?.uuid == BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER) {
                    if (value != null) {
                        if ((value[0] and 1) > 0) { // if notifications enabled
                            if (!running) {
                                connectionStatusChannel.offer(true)
                                Timber.d("Notifications enabled, starting packet writer")
                                running = true
                                GlobalScope.launch(Dispatchers.IO) { packetWriter() }
                            }
                            if (!bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value)) {
                                Timber.e("Failed to send confirm for descriptor write")
                                closePebble()
                            }
                        } else {
                            Timber.d("Device requested disable notifications")
                            closePebble()
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
                sendPending?.complete(GattStatus(status).isSuccess())
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
                            if (targetDevice.address == device.address && running) {
                                Timber.d("Device disconnected, closing")
                                isConnected = false
                                closePebble()
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun initServer(): Boolean {
        val bluetoothManager = context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
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
        if (bluetoothGattServer.addService(gattService) && serverReady.await()) {
            Timber.d("Server set up and ready for connection")
        } else {
            Timber.e("Failed to add service")
            return false
        }
        return true
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

    private suspend fun attemptWrite(packet: GATTPacket) {
        if (packet.type == GATTPacket.PacketType.DATA || packet.type == GATTPacket.PacketType.RESET) ackPending[packet.sequence] = CompletableDeferred(packet)
        var success = false
        var attempt = 0
        while (!success && attempt < 3) {
            requestWritePacket(packet.data)
            if (packet.type == GATTPacket.PacketType.DATA || packet.type == GATTPacket.PacketType.RESET) {
                try {
                    withTimeout(1000) {
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

    private suspend fun packetWriter() = coroutineScope {
        launch(Dispatchers.IO) {
            val buf: ByteBuffer = ByteBuffer.allocate(mtu)
            while (true) {
                packetWriteInputStream.readFully(buf, 0, 4)
                val metBuf = ByteBuffer.wrap(buf.array())
                metBuf.order(ByteOrder.BIG_ENDIAN)
                val length = metBuf.short
                val endpoint = metBuf.short
                if (length < 0) {
                    Timber.w("Invalid length in packet (EP ${ProtocolEndpoint.getByValue(endpoint.toUShort())}): got $length")
                    continue
                }
                Timber.d("Writing packet of EP ${ProtocolEndpoint.getByValue(endpoint.toUShort())} length $length")
                var written = 0
                val count = length.toInt().coerceAtMost(mtu)
                packetWriteInputStream.readFully(buf, 4, count - 4)
                var thisSeq = getSeq()
                attemptWrite(GATTPacket(GATTPacket.PacketType.DATA, thisSeq, buf.array()))
                written += count
                buf.rewind()
                while (length - written > 0) {
                    packetWriteInputStream.readFully(buf, 0, count)
                    thisSeq = getSeq()
                    attemptWrite(GATTPacket(GATTPacket.PacketType.DATA, thisSeq, buf.array()))
                    written += count
                    buf.rewind()
                }
            }
        }
    }

    private suspend fun requestWritePacket(data: ByteArray): Boolean {
        sendPending?.await()
        dataCharacteristic.value = data
        sendPending = CompletableDeferred()
        return bluetoothGattServer.notifyCharacteristicChanged(targetDevice, dataCharacteristic, false)
    }

    private suspend fun sendAck(sequence: UShort, reset: Boolean = false) {
        Timber.d("Sending ACK for $sequence")
        requestWritePacket(GATTPacket(if (reset) GATTPacket.PacketType.RESET_ACK else GATTPacket.PacketType.ACK, sequence).data)
    }

    override suspend fun requestReset() {
        val thisSeq = getSeq()
        attemptWrite(GATTPacket(GATTPacket.PacketType.RESET, thisSeq))
    }

    override suspend fun connectPebble(): Boolean {
        return connectionStatusChannel.receive()
    }

    fun closePebble() {
        Timber.d("Server closing connection")
        connectionStatusChannel.offer(false)
        packetOutputStream.close()
        inputStream.close()
        bluetoothGattServer.close()
    }

}