package io.rebble.fossil.bluetooth

/*import android.bluetooth.*
import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.experimental.and

class BlueGATTServer(val context: Context, val targetDevice: BluetoothDevice, private val gattPacketCallback: suspend (GATTPacket) -> Unit) : BlueGATTIO {
    override var isConnected = false
    var bluetoothGattServer: BluetoothGattServer? = null
    var dataCharacteristic: BluetoothGattCharacteristic? = null

    private var mtu = 23
    private var seq: Short = 0

    private fun getSeq(): Short {
        seq++
        if (seq > 31) seq = 0
        return seq
    }

    fun openServer() {
        if (bluetoothGattServer == null) {
            val bluetoothManager = context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothGattServer = bluetoothManager.openGattServer(context, bluetoothGattServerCallbacks)
            if (bluetoothGattServer != null) {
                val gattService = BluetoothGattService(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER, BluetoothGattService.SERVICE_TYPE_PRIMARY)
                gattService.addCharacteristic(BluetoothGattCharacteristic(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED))

                dataCharacteristic = BluetoothGattCharacteristic(BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_READ,
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED)
                dataCharacteristic!!.addDescriptor(BluetoothGattDescriptor(BlueGATTConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR, BluetoothGattDescriptor.PERMISSION_WRITE))
                gattService.addCharacteristic(dataCharacteristic)

                bluetoothGattServer?.addService(gattService)
                if (bluetoothGattServer?.getService(gattService.uuid) != null) {
                    Timber.i("Server started successfully")
                }
            }
        }
    }

    override fun sendPacket(bytes: ByteArray, callback: (Boolean) -> Unit) {
        val packet = GATTPacket(GATTPacket.PacketType.DATA, getSeq(), bytes)
        if (!notifyDevice(packet.toByteArray())) {
            callback(false)
        } else {
            //TODO Callback
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {

    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {

    }

    override fun setMTU(newMTU: Int) {
        this.mtu = newMTU
    }

    override fun requestReset() {
        notifyDevice(GATTPacket(GATTPacket.PacketType.RESET, getSeq()).toByteArray())
    }

    val bluetoothGattServerCallbacks = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            if (device != null && device.address != targetDevice?.address) return
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Timber.w("ConnectionState error status of ${status}")
            }
            if (newState == BluetoothGattServer.STATE_CONNECTED) {
                Timber.i("Device connected to server")
            } else if (newState == BluetoothGattServer.STATE_DISCONNECTED) {
                Timber.i("Device disconnected from server")
            }
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            if (device?.address != targetDevice.address) return
            Timber.d("onDescriptorWriteRequest") //TODO: Remove
            if (descriptor?.characteristic?.uuid == BlueGATTConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_READ) {
                if (value == null) {
                    Timber.e("GATT packet was null")
                } else {
                    val notifsEnabled = value[0] and 1 > 0
                    if (!notifsEnabled) {
                        Timber.w("Notifications disabled, disconnecting")
                        TODO("Disconnect")
                    }
                }
            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            if (device?.address != targetDevice?.address) return
            if (value == null) {
                Timber.e("Received packet was null")
                return;
            }
            Timber.d("Got packet back") //TODO: Remove

            GlobalScope.launch { gattPacketCallback(GATTPacket(value)) } //XXX
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            if (device?.address != targetDevice?.address) return
            Timber.d("onCharacteristicReadRequest") //TODO: Remove

            if (characteristic?.uuid == BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID) {
                val metaResponse = byteArrayOf(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1) // minSupportedGattVer, maxSupportedGattVer, ??
                if (!(bluetoothGattServer?.sendResponse(device, requestId, 0, offset, metaResponse)
                                ?: false)) {
                    Timber.e("Error sending response!")
                    TODO("Disconnect and request reconnect?")
                }
            }
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            if (service?.uuid == BlueGATTConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER && status == BluetoothGatt.GATT_SUCCESS) {
                Timber.d("Added GATT device service")
                val padService = BluetoothGattService(BlueGATTConstants.UUIDs.FAKE_SERVICE_UUID, 0)
                padService.addCharacteristic(BluetoothGattCharacteristic(BlueGATTConstants.UUIDs.FAKE_SERVICE_UUID, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ))
                bluetoothGattServer?.addService(padService)
            }
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            if (device?.address != targetDevice?.address) return
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Timber.v("Sent packet successfully")
            }
        }
    }

    override fun connectPebble(): Boolean {
        return bluetoothGattServer != null //TODO
    }

    private fun notifyDevice(bytes: ByteArray): Boolean {
        dataCharacteristic?.setValue(bytes)
        return bluetoothGattServer?.notifyCharacteristicChanged(targetDevice, dataCharacteristic, false)
                ?: false
    }
}*/