package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import io.rebble.cobble.bluetooth.ble.util.GattCharacteristicBuilder
import io.rebble.cobble.bluetooth.ble.util.GattDescriptorBuilder
import io.rebble.cobble.bluetooth.ble.util.GattServiceBuilder
import io.rebble.libpebblecommon.ble.LEConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

class PPoGService(private val scope: CoroutineScope) : GattService {
    private val dataCharacteristic = GattCharacteristicBuilder()
            .withUuid(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER))
            .withProperties(BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
            .withPermissions(BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED)
            .addDescriptor(
                    GattDescriptorBuilder()
                            .withUuid(UUID.fromString(LEConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR))
                            .withPermissions(BluetoothGattCharacteristic.PERMISSION_WRITE)
                            .build()
            )
            .build()

    private val metaCharacteristic = GattCharacteristicBuilder()
            .withUuid(UUID.fromString(LEConstants.UUIDs.META_CHARACTERISTIC_SERVER))
            .withProperties(BluetoothGattCharacteristic.PROPERTY_READ)
            .withPermissions(BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED)
            .build()

    private val bluetoothGattService = GattServiceBuilder()
            .withType(BluetoothGattService.SERVICE_TYPE_PRIMARY)
            .withUuid(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER))
            .addCharacteristic(metaCharacteristic)
            .addCharacteristic(dataCharacteristic)
            .build()

    private val ppogConnections = mutableMapOf<String, PPoGServiceConnection>()

    /**
     * Filter flow for events related to a specific device
     * @param deviceAddress Address of the device to filter for
     * @return Function to filter events, used in [Flow.filter]
     */
    private fun filterFlowForDevice(deviceAddress: String) = { event: ServerEvent ->
        when (event) {
            is ConnectionStateEvent -> event.device.address == deviceAddress
            else -> false
        }
    }

    private suspend fun runService(eventFlow: Flow<ServerEvent>) {
        eventFlow.collect {
            when (it) {
                is ConnectionStateEvent -> {
                    Timber.d("Connection state changed: ${it.newState} for device ${it.device.address}")
                    if (it.newState == BluetoothGatt.STATE_CONNECTED) {
                        check(ppogConnections[it.device.address] == null) { "Connection already exists for device ${it.device.address}" }
                        if (ppogConnections.isEmpty()) {
                            val connection = PPoGServiceConnection(this, it.device)
                            connection.start(eventFlow
                                    .filterIsInstance<ServiceEvent>()
                                    .filter(filterFlowForDevice(it.device.address))
                            )
                            ppogConnections[it.device.address] = connection
                        } else {
                            //TODO: Handle multiple connections
                            Timber.w("Multiple connections not supported yet")
                        }
                    } else if (it.newState == BluetoothGatt.STATE_DISCONNECTED) {
                        ppogConnections[it.device.address]?.close()
                        ppogConnections.remove(it.device.address)
                    }
                }
            }
        }
    }

    override fun register(eventFlow: Flow<ServerEvent>): BluetoothGattService {
        scope.launch {
            runService(eventFlow)
        }
        return bluetoothGattService
    }

}