package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import java.nio.ByteBuffer

class ConnectionParamManager(val gatt: BluetoothGatt, val onParamsComplete: () -> Unit) {
    private val logTag = "ConnectionParamManager"

    enum class ManagerStatus {
        UNSUBSCRIBED,
        SUBSCRIBING,
        SUBSCRIBED_REQUESTING,
        DONE
    }

    private var managerStatus = ManagerStatus.UNSUBSCRIBED

    fun subscribe(): Boolean {
        if (managerStatus != ManagerStatus.UNSUBSCRIBED) {
            Log.e(logTag, "Tried subscribing when already subscribed/subscribing")
            return false
        } else {
            val service = gatt.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)
            if (service == null) {
                Log.e(logTag, "Pairing service null")
                return false
            } else {
                val characteristic = service.getCharacteristic(BlueGATTConstants.UUIDs.CONNECTION_PARAMETERS_CHARACTERISTIC)
                if (characteristic == null) {
                    Log.e(logTag, "Conn params characteristic null")
                    return false
                } else {
                    val configDescriptor = characteristic.getDescriptor(BlueGATTConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR)

                    configDescriptor.setValue(BlueGATTConstants.CHARACTERISTIC_SUBSCRIBE_VALUE)
                    if (!gatt.writeDescriptor(configDescriptor)) {
                        Log.e(logTag, "Failed to write subscribe value")
                        return false
                    } else if (!gatt.setCharacteristicNotification(characteristic, true)) {
                        Log.e(logTag, "BluetoothGatt refused to subscribe")
                        return false
                    } else {
                        managerStatus = ManagerStatus.SUBSCRIBING
                        return true
                    }
                }
            }
        }
    }

    fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        if (managerStatus == ManagerStatus.SUBSCRIBING && descriptor?.characteristic?.uuid == BlueGATTConstants.UUIDs.CONNECTION_PARAMETERS_CHARACTERISTIC) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                managerStatus = ManagerStatus.SUBSCRIBED_REQUESTING
                Log.d(logTag, "Requesting mgmt settings from pebble")
                val mgmtData = ByteBuffer.allocate(2)
                mgmtData.put(0)
                mgmtData.put(0) // disablePebbleParamManagement

                val service = gatt?.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)
                if (service == null) {
                    Log.e(logTag, "Pairing service null")
                } else {
                    val characteristic = service.getCharacteristic(BlueGATTConstants.UUIDs.CONNECTION_PARAMETERS_CHARACTERISTIC)
                    if (characteristic == null) {
                        Log.e(logTag, "Characteristic null")
                    } else {
                        characteristic.setValue(mgmtData.array())
                        if (!gatt.writeCharacteristic(characteristic)) {
                            Log.e(logTag, "Failed to request mgmt settings (write failed)")
                        }
                    }
                }
            } else {
                Log.e(logTag, "Failed to subscribe")
            }
        }
    }

    fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        if (characteristic?.uuid == BlueGATTConstants.UUIDs.CONNECTION_PARAMETERS_CHARACTERISTIC) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                managerStatus = ManagerStatus.DONE
                onParamsComplete()
            }
        }
    }
}