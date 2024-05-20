package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService

interface ServerEvent
class ServiceAddedEvent(val status: Int, val service: BluetoothGattService?) : ServerEvent
class ServerInitializedEvent(val server: BluetoothGattServer) : ServerEvent

open class ServiceEvent(val device: BluetoothDevice) : ServerEvent
class ConnectionStateEvent(device: BluetoothDevice, val status: Int, val newState: Int) : ServiceEvent(device)
class CharacteristicReadEvent(device: BluetoothDevice, val requestId: Int, val offset: Int, val characteristic: BluetoothGattCharacteristic, val respond: (CharacteristicResponse) -> Unit) : ServiceEvent(device)
class CharacteristicWriteEvent(device: BluetoothDevice, val requestId: Int, val characteristic: BluetoothGattCharacteristic, val preparedWrite: Boolean, val responseNeeded: Boolean, val offset: Int, val value: ByteArray, val respond: (Int) -> Unit) : ServiceEvent(device)
class CharacteristicResponse(val status: Int, val offset: Int, val value: ByteArray)