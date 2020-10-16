package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic

interface BlueGATTIO {
    var isConnected: Boolean
    fun sendPacket(bytes: ByteArray, callback: (Boolean) -> Unit)
    fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?)
    fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int)
    fun setMTU(newMTU: Int)
    fun requestReset()
    fun connectPebble(): Boolean
}