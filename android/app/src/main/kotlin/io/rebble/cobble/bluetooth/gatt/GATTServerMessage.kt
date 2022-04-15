package io.rebble.cobble.bluetooth.gatt

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService

open class GATTServerMessage {
    data class CharacteristicWriteRequest(
            val device: BluetoothDevice?,
            val requestId: Int,
            val characteristic: BluetoothGattCharacteristic?,
            val preparedWrite: Boolean,
            val responseNeeded: Boolean,
            val offset: Int,
            val value: ByteArray?
    ) : GATTServerMessage() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CharacteristicWriteRequest

            if (device != other.device) return false
            if (requestId != other.requestId) return false
            if (characteristic != other.characteristic) return false
            if (preparedWrite != other.preparedWrite) return false
            if (responseNeeded != other.responseNeeded) return false
            if (offset != other.offset) return false
            if (value != null) {
                if (other.value == null) return false
                if (!value.contentEquals(other.value)) return false
            } else if (other.value != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = device?.hashCode() ?: 0
            result = 31 * result + requestId
            result = 31 * result + (characteristic?.hashCode() ?: 0)
            result = 31 * result + preparedWrite.hashCode()
            result = 31 * result + responseNeeded.hashCode()
            result = 31 * result + offset
            result = 31 * result + (value?.contentHashCode() ?: 0)
            return result
        }
    }

    data class CharacteristicReadRequest(
            val device: BluetoothDevice?,
            val requestId: Int,
            val offset: Int,
            val characteristic: BluetoothGattCharacteristic?
    ) : GATTServerMessage()

    data class DescriptorWriteRequest(
            val device: BluetoothDevice?,
            val requestId: Int,
            val descriptor: BluetoothGattDescriptor?,
            val preparedWrite: Boolean,
            val responseNeeded: Boolean,
            val offset: Int,
            val value: ByteArray?
    ) : GATTServerMessage() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DescriptorWriteRequest

            if (device != other.device) return false
            if (requestId != other.requestId) return false
            if (descriptor != other.descriptor) return false
            if (preparedWrite != other.preparedWrite) return false
            if (responseNeeded != other.responseNeeded) return false
            if (offset != other.offset) return false
            if (value != null) {
                if (other.value == null) return false
                if (!value.contentEquals(other.value)) return false
            } else if (other.value != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = device?.hashCode() ?: 0
            result = 31 * result + requestId
            result = 31 * result + (descriptor?.hashCode() ?: 0)
            result = 31 * result + preparedWrite.hashCode()
            result = 31 * result + responseNeeded.hashCode()
            result = 31 * result + offset
            result = 31 * result + (value?.contentHashCode() ?: 0)
            return result
        }
    }

    data class DescriptorReadRequest(
            val device: BluetoothDevice?,
            val requestId: Int,
            val offset: Int,
            val descriptor: BluetoothGattDescriptor?
    ) : GATTServerMessage()

    data class ServiceAdded(
            val status: Int,
            val service: BluetoothGattService?
    ) : GATTServerMessage()

    data class NotificationSent(
            val device: BluetoothDevice?,
            val status: Int
    ) : GATTServerMessage()

    data class ConnectionStateChange(
            val device: BluetoothDevice?,
            val status: Int,
            val newState: Int
    ) : GATTServerMessage()

    data class MtuChanged(
            val device: BluetoothDevice?,
            val mtu: Int
    ) : GATTServerMessage()
}