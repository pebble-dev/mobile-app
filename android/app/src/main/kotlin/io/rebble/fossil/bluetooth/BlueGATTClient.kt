package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.lang.Exception
import java.util.concurrent.locks.ReentrantLock

class BlueGATTClient(private var gatt: BluetoothGatt) : BlueGATTIO {
    private val logTag = "BlueGATTClient"
    val sendLock = ReentrantLock()
    private lateinit var dataCharacteristic: BluetoothGattCharacteristic

    private val packetReadChannel = Channel<GATTPacket>()
    private val packetWriteChannel = Channel<GATTPacket>()

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

    private suspend fun packetWriter() = coroutineScope {
        launch(Dispatchers.IO) {
            while (true) {
                val packet = packetWriteChannel.receive()
                sendLock.lock()
                dataCharacteristic.value = packet.toByteArray()
                if (!gatt.writeCharacteristic(dataCharacteristic)) {
                    Log.e(logTag, "Failed to write to data characteristic")
                }
            }
        }
    }

    suspend fun waitForAck(ackSeq: Short, ackType: GATTPacket.PacketType): Boolean = runBlocking<Boolean> {
        try {
            return@runBlocking withTimeout(4000L) {
                var packet = packetReadChannel.receive()
                while (packet.sequence != ackSeq) {
                    packetReadChannel.offer(packet)
                    packet = packetReadChannel.receive()
                }
                return@withTimeout true
            }
        } catch (e: TimeoutCancellationException) {
            return@runBlocking false
        } catch (e: CancellationException) {
            return@runBlocking false
        }
    }

    suspend fun sendBytesToDevice(bytes: ByteArray): Boolean {
        if (bytes.size + 1 > mtu) {
            Log.e(logTag, "Data too large for MTU")
            return false
        }
        val thisSeq = getSeq()
        packetWriteChannel.offer(GATTPacket(GATTPacket.PacketType.DATA, thisSeq, bytes))
        return waitForAck(thisSeq, GATTPacket.PacketType.ACK)
    }

    override fun requestReset() {
        val thisSeq = getSeq()
        packetWriteChannel.offer(GATTPacket(GATTPacket.PacketType.RESET, thisSeq))
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        if (!isConnected) return
        if (characteristic?.uuid == dataCharacteristic.uuid) {
            sendLock.unlock()
            if (status != BluetoothGatt.GATT_SUCCESS) Log.e(logTag, "Data characteristic write failed!")
        }
    }


    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        if (characteristic?.uuid == BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC && characteristic != null) {
            packetReadChannel.offer(GATTPacket(characteristic.value))
        }
    }

    override fun sendPacket(bytes: ByteArray, callback: (Boolean) -> Unit) {
        GlobalScope.launch {
            callback(sendBytesToDevice(bytes))
        }
    }

    override fun connectPebble(): Boolean {
        val service = gatt.getService(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID)
        if (service == null) {
            Log.e(logTag, "GATT device service null")
            return false
        } else {
            val _dataCharacteristic = service.getCharacteristic(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC)
            if (_dataCharacteristic == null) {
                Log.e(logTag, "GATT device characteristic null")
                return false
            }
            dataCharacteristic = _dataCharacteristic
            GlobalScope.launch { packetWriter() }
        }

        val configDescriptor = dataCharacteristic.getDescriptor(BlueGATTConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR)
        if (configDescriptor == null) {
            Log.e(logTag, "Data characteristic config descriptor null")
            return false
        }
        configDescriptor.setValue(BlueGATTConstants.CHARACTERISTIC_SUBSCRIBE_VALUE)
        if (!gatt.writeDescriptor(configDescriptor)) {
            Log.e(logTag, "Failed to subscribe to data characteristic")
            return false
        } else if (!gatt.setCharacteristicNotification(dataCharacteristic, true)) {
            Log.e(logTag, "Failed to set notify on data characteristic")
            return false
        } else {
            Log.e(logTag, "Success but not because we're not finished!!")
            isConnected = true
            return true
        }
    }
}