package io.rebble.cobble.bluetooth.ble.util

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import java.util.UUID

class GattCharacteristicBuilder {
    private var uuid: UUID? = null
    private var properties: Int = 0
    private var permissions: Int = 0
    private val descriptors = mutableListOf<BluetoothGattDescriptor>()

    fun withUuid(uuid: UUID): GattCharacteristicBuilder {
        this.uuid = uuid
        return this
    }

    fun withProperties(vararg properties: Int): GattCharacteristicBuilder {
        this.properties = properties.reduce { acc, i -> acc or i }
        return this
    }

    fun withPermissions(vararg permissions: Int): GattCharacteristicBuilder {
        this.permissions = permissions.reduce { acc, i -> acc or i }
        return this
    }

    fun addDescriptor(descriptor: BluetoothGattDescriptor): GattCharacteristicBuilder {
        descriptors.add(descriptor)
        return this
    }

    fun build(): BluetoothGattCharacteristic {
        check(uuid != null) { "UUID must be set" }
        val characteristic = BluetoothGattCharacteristic(uuid, properties, permissions)
        descriptors.forEach {
            characteristic.addDescriptor(it)
        }
        return characteristic
    }
}