package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService

interface ServerEvent
class ServiceAddedEvent(val status: Int, val service: BluetoothGattService?) : ServerEvent
class ServerInitializedEvent(val server: GattServer) : ServerEvent

open class ServiceEvent(val device: BluetoothDevice) : ServerEvent
class ConnectionStateEvent(device: BluetoothDevice, val status: Int, val newState: GattConnectionState) : ServiceEvent(device)
enum class GattConnectionState(val value: Int) {
    Disconnected(BluetoothGatt.STATE_DISCONNECTED),
    Connecting(BluetoothGatt.STATE_CONNECTING),
    Connected(BluetoothGatt.STATE_CONNECTED),
    Disconnecting(BluetoothGatt.STATE_DISCONNECTING);

    companion object {
        fun fromInt(value: Int): GattConnectionState {
            return entries.firstOrNull { it.value == value } ?: throw IllegalArgumentException("Unknown connection state: $value")
        }
    }
}
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