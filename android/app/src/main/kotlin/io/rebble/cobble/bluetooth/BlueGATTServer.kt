package io.rebble.cobble.bluetooth

import android.bluetooth.*
import android.content.Context
import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import io.rebble.libpebblecommon.protocolhelpers.ProtocolEndpoint
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.IOException
import java.io.InterruptedIOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.and

class BlueGATTServer(private val targetDevice: BluetoothDevice, private val context: Context) : BluetoothGattServerCallback() {
    private val serverReady = CompletableDeferred<Boolean>()
    private val connectionStatusChannel = Channel<Boolean>(0)

    private val ackPending: MutableMap<Int, CompletableDeferred<GATTPacket>> = mutableMapOf()

    private var sendMutex = Mutex()
    private var readMutex = Mutex()
    private var writerCoroutine: Job? = null
    private var writerRunning = AtomicBoolean()

    private var mtu = BlueGATTConstants.DEFAULT_MTU
    private var seq: Int = 0
    private var remoteSeq: Int = 0
    private var lastAck: GATTPacket? = null

    private lateinit var bluetoothGattServer: BluetoothGattServer
    private lateinit var dataCharacteristic: BluetoothGattCharacteristic

    val inputStream = PipedInputStream()
    val packetOutputStream = PipedOutputStream(inputStream)

    private val packetWriteInputStream = PipedInputStream()
    val outputStream = PipedOutputStream(packetWriteInputStream)

    var connected = false

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
                GlobalScope.launch(Dispatchers.IO) {
                    val packet = GATTPacket(value)
                    when (packet.type) {
                        GATTPacket.PacketType.RESET_ACK, GATTPacket.PacketType.ACK -> {
                            ackPending.remove(packet.sequence)?.complete(packet)
                            Timber.d("Got ACK for ${packet.sequence}")
                        }
                        GATTPacket.PacketType.DATA -> {
                            readMutex.withLock {
                                val expected = getExpectedRemoteSeq()
                                Timber.d("Packet ${packet.sequence}, Expected $expected")
                                if (packet.sequence == expected) {
                                    try {
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
                                        remoteSeq = lastAck!!.sequence + 1
                                        sendAck(lastAck!!.sequence)
                                    }else {
                                        requestReset()
                                    }
                                }
                            }
                        }
                        GATTPacket.PacketType.RESET -> {
                            if (seq != 0) Timber.w("Got reset on non zero sequence")
                            reset()
                            sendAck(packet.sequence, true)
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
                    GlobalScope.launch(Dispatchers.IO) {
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
            sendMutex.unlock()
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
                        if (targetDevice.address == device.address && writerCoroutine != null) {
                            Timber.d("Device disconnected, closing")
                            closePebble()
                        }
                    }
                }
            }
        }
    }

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

    private fun getSeq(): Int {
        if (seq == 32) seq = 0
        return seq++
    }

    private fun getExpectedRemoteSeq(): Int {
        if (remoteSeq == 32) remoteSeq = 0
        return remoteSeq++
    }

    fun setMTU(newMTU: Int) {
        this.mtu = newMTU
    }

    private suspend fun attemptWrite(packet: GATTPacket) {
        if (packet.type == GATTPacket.PacketType.DATA || packet.type == GATTPacket.PacketType.RESET) ackPending[packet.sequence] = CompletableDeferred(packet)
        var success = false
        var attempt = 0
        while (!success && attempt < 3) {
            if (!requestWritePacket(packet.data)) {
                Timber.w("requestWritePacket failed")
                continue
            }
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

    private suspend fun packetWriter() {
        Timber.d("packetWriter launched")
        while (true) {
            if (packetWriteInputStream.available() > 0) {
                val count = packetWriteInputStream.available().coerceAtMost(mtu-2)
                val buf = ByteBuffer.allocate(count)
                packetWriteInputStream.readFully(buf, 0, count)
                attemptWrite(GATTPacket(GATTPacket.PacketType.DATA, getSeq(), buf.array()))
                Timber.d("Wrote $count bytes")
            }else {
                delay(10)
            }
        }
    }

    private suspend fun requestReset() {
        Timber.w("Requesting reset")
        attemptWrite(GATTPacket(GATTPacket.PacketType.RESET, getSeq()))
        reset()
    }

    private suspend fun requestWritePacket(data: ByteArray): Boolean {
        try {
            return withTimeout(5000) {
                sendMutex.lock()
                dataCharacteristic.value = data
                return@withTimeout bluetoothGattServer.notifyCharacteristicChanged(targetDevice, dataCharacteristic, false)
            }
        } catch (e: CancellationException) {
            Timber.w("Failed to acquire lock, timed out.")
            return false
        }
    }

    private suspend fun reset() {
        Timber.d("Resetting LE")
        writerCoroutine?.cancel()
        packetWriteInputStream.skip(packetWriteInputStream.available().toLong())
        ackPending.forEach{
            it.value.cancel()
        }
        ackPending.clear()
        remoteSeq = 0
        seq = 0
        lastAck = null

        val alreadyRunning = writerCoroutine?.isActive == true
        if (alreadyRunning) {
            writerCoroutine?.cancelAndJoin()
        }
        writerCoroutine = GlobalScope.launch(Dispatchers.IO) { packetWriter() }
        if(!alreadyRunning) {
            connectionStatusChannel.send(true)
        }
    }

    private suspend fun sendAck(sequence: Int, reset: Boolean = false) {
        Timber.d("Sending ACK for $sequence")
        val ack = GATTPacket(if (reset) GATTPacket.PacketType.RESET_ACK else GATTPacket.PacketType.ACK, sequence)
        attemptWrite(ack)
        lastAck = ack
    }

    suspend fun connectPebble(): Boolean {
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