package io.rebble.cobble.bluetooth.ble

import io.rebble.libpebblecommon.ble.LEConstants
import timber.log.Timber
import java.nio.ByteBuffer
import java.util.UUID

/**
 * Handles negotiating and reading changes to connection parameters, currently this feature is unused by us so it just tells the pebble to disable it
 */
class ConnectionParamManager(val gatt: BlueGATTConnection) {
    private var subscribed = false

    suspend fun subscribe(): Boolean {
        if (subscribed) {
            Timber.e("Tried subscribing when already subscribed")
        } else {
            val service = gatt.getService(UUID.fromString(LEConstants.UUIDs.PAIRING_SERVICE_UUID))
            if (service == null) {
                Timber.e("Pairing service null")
            } else {
                val characteristic =
                    service.getCharacteristic(
                        UUID.fromString(LEConstants.UUIDs.CONNECTION_PARAMETERS_CHARACTERISTIC)
                    )
                if (characteristic == null) {
                    Timber.e("Conn params characteristic null")
                } else {
                    val configDescriptor =
                        characteristic.getDescriptor(
                            UUID.fromString(
                                LEConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR
                            )
                        )
                    if (gatt.readDescriptor(
                            configDescriptor
                        )?.descriptor?.value.contentEquals(
                            LEConstants.CHARACTERISTIC_SUBSCRIBE_VALUE
                        )
                    ) {
                        Timber.w("Already subscribed to conn params")
                    } else {
                        if (gatt.writeDescriptor(configDescriptor, LEConstants.CHARACTERISTIC_SUBSCRIBE_VALUE)?.isSuccess() == true) {
                            if (gatt.setCharacteristicNotification(characteristic, true)) {
                                val mgmtData = ByteBuffer.allocate(2)
                                mgmtData.put(0)
                                mgmtData.put(1) // disablePebbleParamManagement
                                if (gatt.writeCharacteristic(characteristic, mgmtData.array())?.isSuccess() == true) {
                                    Timber.d("Configured successfully")
                                    return true
                                } else {
                                    Timber.e("Couldn't write conn param config")
                                }
                                return true
                            } else {
                                Timber.e("BluetoothGatt refused to subscribe")
                            }
                        } else {
                            Timber.e("Failed to write subscribe value")
                        }
                    }
                }
            }
        }
        return false
    }
}