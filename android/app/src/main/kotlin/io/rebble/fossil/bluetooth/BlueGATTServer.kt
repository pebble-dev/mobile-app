package io.rebble.fossil.bluetooth

import android.bluetooth.*
import android.content.Context

class BlueGATTServer(val context: Context) : BlueGATTIO {
    override var isConnected = false
    var bluetoothGattServer: BluetoothGattServer? = null
    var dataCharacteristic: BluetoothGattCharacteristic? = null
    override fun sendPacket(bytes: ByteArray, callback: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        TODO("Not yet implemented")
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        TODO("Not yet implemented")
    }

    override fun setMTU(newMTU: Int) {
        TODO("Not yet implemented")
    }

    override fun requestReset() {
        TODO("Not yet implemented")
    }

    val bluetoothGattServerCallbacks = object : BluetoothGattServerCallback() {

    }

    override fun connectPebble(): Boolean {
        if (bluetoothGattServer == null) {
            val bluetoothManager = context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothGattServer = bluetoothManager.openGattServer(context, bluetoothGattServerCallbacks)
            if (bluetoothGattServer != null) {
                val gattService = BluetoothGattService(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
                gattService.addCharacteristic(BluetoothGattCharacteristic(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED))

                dataCharacteristic = BluetoothGattCharacteristic(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC,
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED)
                dataCharacteristic!!.addDescriptor(BluetoothGattDescriptor(BlueGATTConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR, BluetoothGattDescriptor.PERMISSION_WRITE))
                gattService.addCharacteristic(dataCharacteristic)

                bluetoothGattServer?.addService(gattService)
                if (bluetoothGattServer?.getService(gattService.uuid) != null) {
                    return true
                }
            }
        }
        return false
    }
}