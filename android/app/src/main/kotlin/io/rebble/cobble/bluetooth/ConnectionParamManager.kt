package io.rebble.cobble.bluetooth

import timber.log.Timber
import java.nio.ByteBuffer

class ConnectionParamManager(val gatt: BlueGATTConnection) {
    private var subscribed = false

    suspend fun subscribe(): Boolean {
        if (subscribed) {
            Timber.e("Tried subscribing when already subscribed")
        } else {
            val service = gatt.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)
            if (service == null) {
                Timber.e("Pairing service null")
            } else {
                val characteristic = service.getCharacteristic(BlueGATTConstants.UUIDs.CONNECTION_PARAMETERS_CHARACTERISTIC)
                if (characteristic == null) {
                    Timber.e("Conn params characteristic null")
                } else {
                    val configDescriptor = characteristic.getDescriptor(BlueGATTConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR)
                    if (gatt.readDescriptor(configDescriptor)?.descriptor?.value.contentEquals(BlueGATTConstants.CHARACTERISTIC_SUBSCRIBE_VALUE)) {
                        Timber.w("Already subscribed to conn params")
                    }else {
                        if (gatt.writeDescriptor(configDescriptor, BlueGATTConstants.CHARACTERISTIC_SUBSCRIBE_VALUE)?.isSuccess() == true) {
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