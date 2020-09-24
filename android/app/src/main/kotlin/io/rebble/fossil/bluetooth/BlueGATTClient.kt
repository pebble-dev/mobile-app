package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class BlueGATTClient(private val gatt: BluetoothGatt, private val readyForNextPacket: () -> Unit) {
    private val logTag = "GATTClient"
    val sendLock = ReentrantLock()

    private var dataCharacteristic: BluetoothGattCharacteristic? = null

    fun connect(): Boolean {
        val service = gatt.getService(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID)
        if (service == null) {
            Log.e(logTag, "GATT device service null")
            return false
        } else {
            dataCharacteristic = service.getCharacteristic(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC)
            if (dataCharacteristic == null) {
                Log.e(logTag, "GATT device characteristic null")
                return false
            } else {
                val configDescriptor = dataCharacteristic?.getDescriptor(BlueGATTConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR)
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
                    return true
                }
            }
        }
    }

    fun sendBytes(bytes: ByteArray): Boolean {
        var writeWorked = true
        sendLock.lock()
        dataCharacteristic?.value = bytes
        writeWorked = gatt.writeCharacteristic(dataCharacteristic)
        if (!writeWorked) sendLock.unlock()
        return writeWorked
    }

    fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        if (characteristic?.uuid == dataCharacteristic?.uuid) {
            sendLock.unlock()
            if (status != BluetoothGatt.GATT_SUCCESS) Log.e(logTag, "Data characteristic write failed!")
            readyForNextPacket()
        }
    }

    fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        if (characteristic?.uuid == BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC) {

        }
    }
}