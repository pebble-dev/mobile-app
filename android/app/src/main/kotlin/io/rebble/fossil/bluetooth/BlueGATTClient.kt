package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import timber.log.Timber
import java.nio.ByteBuffer
import kotlin.math.ceil
import kotlin.math.min

class BlueGATTClient(private var gatt: BluetoothGatt, private val gattPacketCallback: suspend (GATTPacket) -> Unit) : BlueGATTIO {
    private val logTag = "BlueGATTClient"
    private lateinit var dataReadCharacteristic: BluetoothGattCharacteristic
    private lateinit var dataWriteCharacteristic: BluetoothGattCharacteristic

    private val dataReadChannel = Channel<GATTPacket>(Channel.BUFFERED)
    private val packetWriteChannel = Channel<GATTPacket>(0)

    override var isConnected = false
    private val ackPending: MutableMap<UShort, CompletableDeferred<GATTPacket>> = mutableMapOf()
    private var sendPending: CompletableDeferred<Boolean>? = null

    private var mtu = 23
    private var seq: UShort = 0U
    private var remoteSeq: UShort = 0U

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
        return gatt.writeCharacteristic(dataWriteCharacteristic)
    }

    private suspend fun sendAck(sequence: UShort, reset: Boolean = false) {
        Timber.d("Sending ACK for ${sequence}")
        packetWriteChannel.send(GATTPacket(if (reset) GATTPacket.PacketType.RESET_ACK else GATTPacket.PacketType.ACK, sequence))
    }

    suspend fun sendBytesToDevice(bytes: ByteArray): Boolean {
        val mtu = this.mtu

        val count = ceil(bytes.size.toFloat() / mtu.toFloat()).toInt()
        val buf = ByteBuffer.wrap(bytes)
        for (i in 0..count - 1) {
            val payload = ByteArray(min(mtu, buf.array().size - buf.position()))
            buf.get(payload)

            val thisSeq = getSeq()
            val result = CompletableDeferred<GATTPacket>()
            ackPending[thisSeq] = result
            packetWriteChannel.send(GATTPacket(GATTPacket.PacketType.DATA, thisSeq, payload))
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
            if (!gattStatus.isSuccess()) Timber.e(logTag, "Data characteristic write failed: ${gattStatus}")
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
        val service = gatt.getService(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_CLIENT)
        if (service == null) {
            Timber.e("GATT device service null")
            return false
        } else {
            val _dataReadCharacteristic = service.getCharacteristic(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_READ)
            if (_dataReadCharacteristic == null) {
                Timber.e("GATT device read characteristic null")
                return false
            } else {
                val configDescriptor = _dataReadCharacteristic.getDescriptor(BlueGATTConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR)
                if (configDescriptor == null) {
                    Timber.e("Data characteristic config descriptor null")
                    return false
                }
                configDescriptor.setValue(BlueGATTConstants.CHARACTERISTIC_SUBSCRIBE_VALUE)
                if (!gatt.writeDescriptor(configDescriptor)) {
                    Timber.e("Failed to subscribe to data characteristic")
                    return false
                } else if (!gatt.setCharacteristicNotification(_dataReadCharacteristic, true)) {
                    Timber.e("Failed to set notify on data characteristic")
                    return false
                } else {
                    val _dataWriteCharacteristic = service.getCharacteristic(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_WRITE)
                    if (_dataWriteCharacteristic == null) {
                        Timber.e("GATT device write characteristic null")
                        return false
                    } else {
                        dataReadCharacteristic = _dataReadCharacteristic
                        dataWriteCharacteristic = _dataWriteCharacteristic
                        isConnected = true
                        GlobalScope.launch { packetWriter() }
                        GlobalScope.launch { packetReader() }
                        return true
                    }
                }
            }
        }
    }

}