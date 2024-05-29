package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import kotlinx.coroutines.flow.Flow
import java.io.Closeable

interface GattServer: Closeable {
    fun getServer(): BluetoothGattServer?
    fun getFlow(): Flow<ServerEvent>
    fun isOpened(): Boolean
    suspend fun notifyCharacteristicChanged(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic, confirm: Boolean, value: ByteArray)
}