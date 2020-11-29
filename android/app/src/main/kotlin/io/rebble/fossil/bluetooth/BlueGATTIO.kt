package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothGattCharacteristic
import java.io.PipedInputStream

interface BlueGATTIO {
    var isConnected: Boolean
    suspend fun sendPacket(bytes: ByteArray): Boolean
    fun onCharacteristicChanged(value: ByteArray, characteristic: BluetoothGattCharacteristic?)
    fun setMTU(newMTU: Int)
    fun requestReset()
    suspend fun connectPebble(): Boolean
    val packetInputStream: PipedInputStream
}