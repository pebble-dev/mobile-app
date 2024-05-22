package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import io.rebble.cobble.bluetooth.ble.util.GattCharacteristicBuilder
import io.rebble.cobble.bluetooth.ble.util.GattServiceBuilder
import io.rebble.libpebblecommon.ble.LEConstants
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class DummyService: GattService {
    private val dummyService = GattServiceBuilder()
            .withUuid(UUID.fromString(LEConstants.UUIDs.FAKE_SERVICE_UUID))
            .addCharacteristic(
                    GattCharacteristicBuilder()
                            .withUuid(UUID.fromString(LEConstants.UUIDs.FAKE_SERVICE_UUID))
                            .withProperties(BluetoothGattCharacteristic.PROPERTY_READ)
                            .withPermissions(BluetoothGattCharacteristic.PERMISSION_READ)
                            .build()
            )
            .build()
    override fun register(eventFlow: Flow<ServerEvent>): BluetoothGattService {
        return dummyService
    }
}