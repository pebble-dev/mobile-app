package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import timber.log.Timber
import java.nio.ByteBuffer
import kotlin.math.ceil

class BlueGATTClient(private var gatt: BluetoothGatt, private val gattPacketCallback: suspend (GATTPacket) -> Unit) : BlueGATTIO {
    private val logTag = "BlueGATTClient"
    private lateinit var dataReadCharacteristic: BluetoothGattCharacteristic
    private lateinit var dataWriteCharacteristic: BluetoothGattCharacteristic

    private val ackReadChannel = Channel<GATTPacket>(Channel.BUFFERED)
    private val dataReadChannel = Channel<GATTPacket>(Channel.BUFFERED)
    private val packetWriteChannel = Channel<GATTPacket>(Channel.BUFFERED)
    private val ackWriteChannel = Channel<GATTPacket>(0)

    override var isConnected = false

    private var mtu = 23
    private var seq: Short = 0

    private fun getSeq(): Short {
        seq++
        if (seq > 31) seq = 0
        return seq
    }

    override fun setMTU(newMTU: Int) {
        this.mtu = newMTU
    }

    private suspend fun packetReader() = coroutineScope {
        launch(Dispatchers.IO) {
            while (true) {
                val packet = dataReadChannel.receive()
                sendAck(packet.sequence)
                gattPacketCallback(packet)
            }
        }
    }

    private suspend fun packetWriter() = coroutineScope {
        launch(Dispatchers.IO) {
            while (true) {
                val packet = packetWriteChannel.receive()
                dataWriteCharacteristic.value = packet.toByteArray()
                if (!gatt.writeCharacteristic(dataWriteCharacteristic)) {
                    Timber.e("Failed to write to data characteristic")
                    //TODO: retry/disconnect?
                }
            }
        }
    }

    private suspend fun ackWriter() = coroutineScope {
        launch(Dispatchers.IO) {
            while (true) {
                val ack = ackWriteChannel.receive()
                Timber.d("Sending ACK for ${ack.sequence}")
                dataWriteCharacteristic.value = ack.toByteArray()
                var tries = 0
                var success = false
                while (++tries < 3 && success == false) {
                    if (!gatt.writeCharacteristic(dataWriteCharacteristic)) {
                        Timber.e("Failed to write to data characteristic")
                    } else {
                        success = true
                    }
                }
            }
        }
    }

    private suspend fun waitForAck(ackSeq: Short, ackType: GATTPacket.PacketType): Boolean {
        try {
            return withTimeout(10000L) {
                val packet = ackReadChannel.receive()
                return@withTimeout packet.sequence == ackSeq
            }
        } catch (e: TimeoutCancellationException) {
            Timber.e("Timed out waiting for ACK")
            return false
        }
    }

    private suspend fun sendAck(sequence: Short, reset: Boolean = false) {
        ackWriteChannel.send(GATTPacket(if (reset) GATTPacket.PacketType.RESET_ACK else GATTPacket.PacketType.ACK, sequence))
    }

    suspend fun sendBytesToDevice(bytes: ByteArray): Boolean {
        val mtu = this.mtu
        try {
            withTimeout(4000L) {
                while (!ackWriteChannel.isEmpty) {
                    delay(10L)
                }
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w("Timed out waiting for ACK to write, continuing anyways")
        }

        val count = ceil(bytes.size.toFloat() / mtu.toFloat()).toInt()
        val buf = ByteBuffer.wrap(bytes)
        for (i in 0..count - 1) {
            val payload = ByteArray(mtu)
            buf.get(payload)

            val thisSeq = getSeq()
            packetWriteChannel.offer(GATTPacket(GATTPacket.PacketType.DATA, thisSeq, payload))
            if (!waitForAck(thisSeq, GATTPacket.PacketType.ACK)) return false
        }
        return true
    }

    override fun requestReset() {
        val thisSeq = getSeq()
        packetWriteChannel.offer(GATTPacket(GATTPacket.PacketType.RESET, thisSeq))
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        if (!isConnected) return
        if (characteristic?.uuid == dataWriteCharacteristic.uuid) {
            if (status != BluetoothGatt.GATT_SUCCESS) Log.e(logTag, "Data characteristic write failed!")
        }
    }


    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        if (characteristic?.uuid == BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_READ && characteristic != null) {
            val packet = GATTPacket(characteristic.value)
            when (packet.type) {
                GATTPacket.PacketType.RESET_ACK, GATTPacket.PacketType.ACK -> {
                    ackReadChannel.offer(packet)
                }
                GATTPacket.PacketType.DATA -> {
                    dataReadChannel.offer(packet)
                }
                GATTPacket.PacketType.RESET -> {
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
                        GlobalScope.launch { ackWriter() }
                        GlobalScope.launch { packetWriter() }
                        GlobalScope.launch { packetReader() }
                        return true
                    }
                }
            }
        }
    }

}