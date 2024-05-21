package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService

interface ServerEvent
class ServiceAddedEvent(val status: Int, val service: BluetoothGattService?) : ServerEvent
class ServerInitializedEvent(val server: BluetoothGattServer) : ServerEvent

open class ServiceEvent(val device: BluetoothDevice) : ServerEvent
class ConnectionStateEvent(device: BluetoothDevice, val status: Int, val newState: Int) : ServiceEvent(device)
class CharacteristicReadEvent(device: BluetoothDevice, val requestId: Int, val offset: Int, val characteristic: BluetoothGattCharacteristic, val respond: (CharacteristicResponse) -> Unit) : ServiceEvent(device)
class CharacteristicWriteEvent(device: BluetoothDevice, val requestId: Int, val characteristic: BluetoothGattCharacteristic, val preparedWrite: Boolean, val responseNeeded: Boolean, val offset: Int, val value: ByteArray, val respond: (Int) -> Unit) : ServiceEvent(device)
class CharacteristicResponse(val status: Int, val offset: Int, val value: ByteArray) {
    companion object {
        val Failure = CharacteristicResponse(BluetoothGatt.GATT_FAILURE, 0, byteArrayOf())
    }
}
class DescriptorReadEvent(device: BluetoothDevice, val requestId: Int, val offset: Int, val descriptor: BluetoothGattDescriptor, val respond: (DescriptorResponse) -> Unit) : ServiceEvent(device)
class DescriptorWriteEvent(device: BluetoothDevice, val requestId: Int, val descriptor: BluetoothGattDescriptor, val offset: Int, val value: ByteArray, val respond: (Int) -> Unit) : ServiceEvent(device)
class DescriptorResponse(val status: Int, val offset: Int, val value: ByteArray)
class NotificationSentEvent(device: BluetoothDevice, val status: Int) : ServiceEvent(device)
class MtuChangedEvent(device: BluetoothDevice, val mtu: Int) : ServiceEvent(device)