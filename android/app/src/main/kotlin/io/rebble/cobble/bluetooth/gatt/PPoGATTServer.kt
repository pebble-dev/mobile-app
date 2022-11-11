package io.rebble.cobble.bluetooth.gatt

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import io.rebble.cobble.datasources.IncomingPacketsListener
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.ble.GATTPacket;
import io.rebble.libpebblecommon.ble.LEConstants
import io.rebble.libpebblecommon.util.DataBuffer
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.sync.Semaphore
import timber.log.Timber
import java.io.IOException
import java.sql.Time
import java.time.Duration
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.min

class PPoGATTServer (
        private val context: Context,
        private val protocolHandler: ProtocolHandler,
        private val incomingPacketsListener: IncomingPacketsListener
) : GATTServer(context) {
    private val serverScope = CoroutineScope(Dispatchers.IO)
    private var pebbleConnected = Channel<Boolean>(1)
    private lateinit var dataCharacteristic: BluetoothGattCharacteristic

    private val seq = GATTSequence()
    private val remoteSeq = GATTSequence()
    private var currentRXPend = 0
    private var mtu = LEConstants.DEFAULT_MTU

    private var lastAck: GATTPacket? = null
    private var delayedAckJob: Job? = null
    private var dataUpdateJob: Job? = null

    private var initialReset = false
    private var connectionVersion = GATTPacket.PPoGConnectionVersion.ZERO
    private var maxRXWindow = LEConstants.MAX_RX_WINDOW
    private var maxTXWindow = LEConstants.MAX_TX_WINDOW
    private val pendingPackets = mutableListOf<GATTPacket>()
    private val ackPending = mutableMapOf<Int, CompletableDeferred<Unit>>()

    private val packetReadSemaphore = Semaphore(1, 0)
    private val packetWriteSemaphore = Semaphore(1, 0)

    private var pendingLength = 0
    private val receiveBuf = mutableListOf<Byte>()

    var connected = false

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    fun setTarget(device: BluetoothDevice?) {
        if (targetDevice != null && device?.address != targetDevice?.address) {
            bluetoothGattServer.cancelConnection(targetDevice)
        }
        targetDevice = device
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    suspend fun init(): Boolean {
        setTarget(null)
        characteristicReadHandlers[UUID.fromString(LEConstants.UUIDs.META_CHARACTERISTIC_SERVER)] = {ch -> onMetaRead(ch)}
        characteristicSubscriptionHandlers[UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER)] = {ch -> onDataSubscribed(ch)}
        characteristicWriteHandlers[UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER)] = {ch -> ch.value?.let { onWrite(it) }}
        return if (initialReset) { // init'd before
            Timber.d("init'd before, rebooting")
            reset()
            initialReset = false
            pebbleConnected.close()
            pebbleConnected = Channel<Boolean>(1)
            true
        } else {
            initServer() && initService()
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    private suspend fun initService(): Boolean {
        val gattService = BluetoothGattService(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER), BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val metaCharacteristic = BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.META_CHARACTERISTIC_SERVER), BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED)
        gattService.addCharacteristic(metaCharacteristic)
        dataCharacteristic = BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER), BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE or BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED)
        dataCharacteristic.addDescriptor(BluetoothGattDescriptor(UUID.fromString(LEConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR), BluetoothGattDescriptor.PERMISSION_WRITE))
        gattService.addCharacteristic(dataCharacteristic)

        val padService = BluetoothGattService(UUID.fromString(LEConstants.UUIDs.FAKE_SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY)
        padService.addCharacteristic(BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.FAKE_SERVICE_UUID), BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ))

        if (getService(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER)) != null) {
            Timber.w("Service already registered, clearing services and then re-registering")
            clearServices()
        }
        return if (addService(gattService) && addService(padService)) {
            Timber.d("Server set up and ready for connection")
            true
        } else {
            Timber.e("Failed to add service")
            false
        }
    }

    private fun getPacketLength(packet: kotlin.collections.List<Byte>): Int {
        val headBuf = DataBuffer(packet.toByteArray().asUByteArray())
        val length = headBuf.getUShort()
        return length.toInt()
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    private fun onWrite(data: ByteArray) {
        serverScope.launch {
            try {
                withTimeout(10000) {
                    packetReadSemaphore.acquire()
                }
            } catch (e : TimeoutCancellationException) {
                Timber.e("Timed out acquiring read semaphore")
            }
            try {
                withTimeout(5000) {
                    if (data.isNotEmpty()) {
                        val packet = GATTPacket(data)
                        when (packet.type) {
                            GATTPacket.PacketType.DATA -> {
                                if (!initialReset) {
                                    initialReset = true
                                    pebbleConnected.trySend(true)
                                }
                                Timber.d("-> DATA ${packet.sequence}")
                                val cRemoteSeq = remoteSeq.next
                                if (packet.sequence == cRemoteSeq) {
                                    sendAck(packet.sequence)
                                    val protoData = packet.toByteArray().drop(1)
                                    if (receiveBuf.isEmpty()) {
                                        pendingLength = getPacketLength(protoData)
                                    }
                                    receiveBuf.addAll(protoData)

                                    while (receiveBuf.size >= pendingLength+4) {
                                        val range = 0..(pendingLength+3)
                                        val bufSlice = receiveBuf.slice(range)
                                        receiveBuf.removeAll(bufSlice)
                                        incomingPacketsListener.receivedPackets.emit(bufSlice.toByteArray())
                                        protocolHandler.receivePacket(bufSlice.toByteArray().asUByteArray())
                                    }
                                } else {
                                    Timber.w("Unexpected sequence ${packet.sequence}, expected $cRemoteSeq")
                                    if (lastAck != null) {
                                        Timber.i("Sending clarifying ACK ${lastAck?.sequence}")
                                        writePacket(GATTPacket.PacketType.ACK, null, lastAck!!.sequence)
                                    } else {
                                        Timber.w("No previous ACK to send on sequence mismatch")
                                    }
                                }
                            }
                            GATTPacket.PacketType.ACK -> {
                                Timber.d("-> ACK ${packet.sequence}")
                                for (i in 0..packet.sequence) {
                                    ackPending.remove(i)?.complete(Unit)
                                    updateData()
                                }
                            }
                            GATTPacket.PacketType.RESET -> {
                                Timber.d("-> RESET ${packet.sequence}")
                                if (seq.current != 0) {
                                    Timber.e("Got reset on non zero sequence")
                                }

                                connectionVersion = packet.getPPoGConnectionVersion()
                                sendResetAck()
                            }
                            GATTPacket.PacketType.RESET_ACK -> {
                                Timber.d("-> RESETACK ${packet.sequence}")
                                Timber.d("Connection version ${connectionVersion.value}")
                                if (connectionVersion.supportsWindowNegotiation && !packet.hasWindowSizes()) {
                                    Timber.w("FW does not support window sizes in reset complete, reverting to connectionVersion 0")
                                    connectionVersion = GATTPacket.PPoGConnectionVersion.ZERO
                                }

                                if (connectionVersion.supportsWindowNegotiation) {
                                    maxRXWindow = min(packet.getMaxRXWindow().toInt(), LEConstants.MAX_RX_WINDOW.toInt()).toByte()
                                    maxTXWindow = min(packet.getMaxTXWindow().toInt(), LEConstants.MAX_TX_WINDOW.toInt()).toByte()
                                    Timber.i("Windows negotiated: rx = $maxRXWindow, tx = $maxTXWindow")
                                }
                                sendResetAck()
                                if (!initialReset) {
                                    Timber.i("Initial reset, everything is connected now")
                                }
                            }
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                throw Error("Took too long to ingest packet, see cause", e)
            } finally {
                packetReadSemaphore.release()
            }
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    suspend fun requestReset() {
        writePacket(GATTPacket.PacketType.RESET, byteArrayOf(connectionVersion.value), 0)
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    private suspend fun sendAck(sequence: Int) {
        if (!connectionVersion.supportsCoalescedAcking) {
            currentRXPend = 0
            writePacket(GATTPacket.PacketType.ACK, null, sequence)
        } else {
            currentRXPend++
            delayedAckJob?.cancel()
            if (currentRXPend >= maxRXWindow) {
                currentRXPend = 0
                writePacket(GATTPacket.PacketType.ACK, null, sequence)
            } else {
                delayedAckJob = serverScope.launch {
                    delay(500)
                    currentRXPend = 0
                    writePacket(GATTPacket.PacketType.ACK, null, sequence)
                }
            }
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    private suspend fun sendResetAck() {
        val data = if (connectionVersion.supportsWindowNegotiation) {
            byteArrayOf(maxRXWindow, maxTXWindow)
        } else {
            null
        }
        writePacket(GATTPacket.PacketType.RESET_ACK, data, 0)
        reset()
    }

    private fun updateData() {
        dataUpdateJob = serverScope.launch {
            try {
                withTimeout(5000) {
                    packetWriteSemaphore.acquire()
                }
            } catch (e: TimeoutCancellationException) {
                Timber.e("Timed out acquiring write semaphore")
            }
            try {
                for (i in 0..pendingPackets.lastIndex) {
                    val packet = pendingPackets[i]
                    dataCharacteristic.value = packet.data
                    bluetoothGattServer.notifyCharacteristicChanged(targetDevice, dataCharacteristic, false)
                    val result = notificationSentChannel.receive()
                    if (result.status != BluetoothGatt.GATT_SUCCESS) {
                        Timber.w("Non-success value ${result.status} updating data")
                    }
                }
                pendingPackets.clear()
            } finally {
                packetWriteSemaphore.release()
                dataUpdateJob = null
            }
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    private suspend fun updateMeta(packet: GATTPacket) {
        dataCharacteristic.value = packet.data
        bluetoothGattServer.notifyCharacteristicChanged(targetDevice, dataCharacteristic, false)
        val result = notificationSentChannel.receive()
        if (result.status != BluetoothGatt.GATT_SUCCESS) {
            Timber.w("Non-success value ${result.status} updating meta")
        }
    }

    private fun onMetaRead(request: CharacteristicReadRequest) {
        serverScope.launch {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                if (!bluetoothGattServer.sendResponse(request.device, request.requestId, 0, request.offset, LEConstants.SERVER_META_RESPONSE)) {
                    Timber.e("Error sending meta response to device")
                    closePebble()
                } else {
                    connected = true
                    delay(5000)
                    if (!initialReset) {
                        Timber.e("No initial reset from watch after 5s")
                        closePebble()
                    }
                }
            }
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    private fun onDataSubscribed(request: CharacteristicSubscriptionRequest) {
        if (request.notify) {
            Timber.d("Data subscribed")
        } else {
            Timber.e("Data unsubscribed on connection")
            closePebble()
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    private suspend fun writePacket(type: GATTPacket.PacketType, data: ByteArray?, sequence: Int? = null) {
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
                val ackPendingCompletable = CompletableDeferred<Unit>()
                ackPending[packet.sequence] = ackPendingCompletable

                if (ackPending.size >= maxTXWindow) {
                    withTimeout(5000) {
                        ackPendingCompletable.await()
                    }
                }

                pendingPackets.add(packet)
                updateData()
            }
            GATTPacket.PacketType.RESET -> {
                Timber.d("<- RESET ${packet.sequence}")
                updateMeta(packet)
            }
            GATTPacket.PacketType.RESET_ACK -> {
                Timber.d("<- RESETACK ${packet.sequence}")
                updateMeta(packet)
            }
            GATTPacket.PacketType.ACK -> {
                lastAck = packet
                updateMeta(packet)
                Timber.d("<- ACK ${packet.sequence}")
            }
            else -> throw IllegalArgumentException()
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    suspend fun write(rawProtocolPacket: ByteArray) {
        val maxPacketSize = mtu-4
        val chunked = rawProtocolPacket.asList().chunked(maxPacketSize)
        Timber.d("Writing packet of length ${rawProtocolPacket.size} in ${chunked.size} chunks")
        withContext(Dispatchers.IO) {
            chunked.forEach {
                writePacket(GATTPacket.PacketType.DATA, it.toByteArray())
            }
        }
        Timber.d("Done writing")
    }

    private fun reset() {
        Timber.i("Resetting LE")
        remoteSeq.reset()
        seq.reset()
        lastAck = null
        dataUpdateJob?.cancel()
        dataUpdateJob = null
        delayedAckJob?.cancel()
        delayedAckJob = null
        pendingPackets.clear()
        ackPending.clear()
    }

    fun closePebble() {
        setTarget(null)
        pebbleConnected.trySend(false)
    }

    suspend fun waitForPebble(): Boolean {
        return pebbleConnected.receive()
    }

    fun setMTU(newMTU: Int) {
        this.mtu = newMTU
    }
}