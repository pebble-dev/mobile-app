package io.rebble.cobble.bluetooth.ble.util

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import java.util.UUID

class GattServiceBuilder {
    private val characteristics = mutableListOf<BluetoothGattCharacteristic>()
    private var uuid: UUID? = null
    private var type: Int = BluetoothGattService.SERVICE_TYPE_PRIMARY

    fun withUuid(uuid: UUID): GattServiceBuilder {
        this.uuid = uuid
        return this
    }

    fun withType(type: Int): GattServiceBuilder {
        this.type = type
        return this
    }

    fun addCharacteristic(characteristic: BluetoothGattCharacteristic): GattServiceBuilder {
        characteristics.add(characteristic)
        return this
    }

    fun build(): BluetoothGattService {
        check(uuid != null) { "UUID must be set" }
        val service = BluetoothGattService(uuid, type)
        characteristics.forEach { service.addCharacteristic(it) }
        return service
    }
}