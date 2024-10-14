package io.rebble.cobble.bluetooth.ble.util

import android.bluetooth.BluetoothGattDescriptor
import java.util.UUID

class GattDescriptorBuilder {
    private var uuid: UUID? = null
    private var permissions: Int = 0

    fun withUuid(uuid: UUID): GattDescriptorBuilder {
        this.uuid = uuid
        return this
    }

    fun withPermissions(vararg permissions: Int): GattDescriptorBuilder {
        this.permissions = permissions.reduce { acc, i -> acc or i }
        return this
    }

    fun build(): BluetoothGattDescriptor {
        check(uuid != null) { "UUID must be set" }
        val descriptor = BluetoothGattDescriptor(uuid, permissions)
        return descriptor
    }
}