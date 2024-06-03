package io.rebble.cobble.bluetooth.ble

import android.bluetooth.*
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*
import kotlin.coroutines.CoroutineContext

@FlowPreview
/**
 * Connect to BT device GATT via coroutines instead of callbacks
 * @param context application context
 * @param cbTimeout the timeout for all callbacks behind the scenes before the suspend function returns null
 */
suspend fun BluetoothDevice.connectGatt(
        context: Context,
        unbindOnTimeout: Boolean,
        auto: Boolean = false,
        cbTimeout: Long = 8000,
        ioDispatcher: CoroutineContext = Dispatchers.IO
): BlueGATTConnection? {
    return BlueGATTConnection(this, cbTimeout, ioDispatcher).connectGatt(context, auto, unbindOnTimeout)
}

class BlueGATTConnection(val device: BluetoothDevice, private val cbTimeout: Long, private val ioDispatcher: CoroutineContext = Dispatchers.IO) : BluetoothGattCallback() {
    var gatt: BluetoothGatt? = null

    private val _connectionStateChanged = MutableStateFlow<ConnectionStateResult?>(null)
    val connectionStateChanged = _connectionStateChanged.filterNotNull()

    private val _characteristicChanged = MutableStateFlow<CharacteristicResult?>(null)
    val characteristicChanged = _characteristicChanged.filterNotNull()

    private val _characteristicRead = MutableStateFlow<CharacteristicResult?>(null)
    val characteristicRead = _characteristicRead.filterNotNull()

    private val _characteristicWritten = MutableStateFlow<CharacteristicResult?>(null)
    val characteristicWritten = _characteristicWritten.filterNotNull()

    private val _descriptorRead = MutableStateFlow<DescriptorResult?>(null)
    val descriptorRead = _descriptorRead.filterNotNull()

    private val _descriptorWritten = MutableStateFlow<DescriptorResult?>(null)
    val descriptorWritten = _descriptorWritten.filterNotNull()

    private val _servicesDiscovered = MutableStateFlow<StatusResult?>(null)
    val servicesDiscovered = _servicesDiscovered.filterNotNull()

    private val _mtuChanged = MutableStateFlow<MTUResult?>(null)
    val mtuChanged = _mtuChanged.filterNotNull()

    open class BlueGATTResult(val gatt: BluetoothGatt?)

    open class StatusResult(gatt: BluetoothGatt?, val status: Int) : BlueGATTResult(gatt) {
        fun isSuccess() = status == BluetoothGatt.GATT_SUCCESS
    }

    class ConnectionStateResult(gatt: BluetoothGatt?, status: Int, val newState: Int) : StatusResult(gatt, status)
    class CharacteristicResult(gatt: BluetoothGatt?, val characteristic: BluetoothGattCharacteristic?, val value: ByteArray? = null, status: Int = BluetoothGatt.GATT_SUCCESS) : StatusResult(gatt, status)
    class DescriptorResult(gatt: BluetoothGatt?, val descriptor: BluetoothGattDescriptor?, status: Int = BluetoothGatt.GATT_SUCCESS, value: ByteArray? = null) : StatusResult(gatt, status)
    class MTUResult(gatt: BluetoothGatt?, val mtu: Int, status: Int) : StatusResult(gatt, status)

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (this.gatt?.device?.address == null || gatt?.device?.address != this.gatt!!.device.address) return
        _connectionStateChanged.value = ConnectionStateResult(gatt, status, newState)
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        if (this.gatt?.device?.address == null || gatt.device?.address != this.gatt!!.device.address) return
        _characteristicChanged.value = CharacteristicResult(gatt, characteristic, value)
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
        if (this.gatt?.device?.address == null || gatt.device?.address != this.gatt!!.device.address) return
        _characteristicRead.value = CharacteristicResult(gatt, characteristic, value, status)
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        if (this.gatt?.device?.address == null || gatt?.device?.address != this.gatt!!.device.address) return
        _characteristicWritten.value = CharacteristicResult(gatt, characteristic, status = status)
    }

    override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int, value: ByteArray) {
        if (this.gatt?.device?.address == null || gatt.device?.address != this.gatt!!.device.address) return
        _descriptorRead.value = DescriptorResult(gatt, descriptor, status, value)
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        if (this.gatt?.device?.address == null || gatt?.device?.address != this.gatt!!.device.address) return
        _descriptorWritten.value = DescriptorResult(gatt, descriptor, status)
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        if (this.gatt?.device?.address == null || gatt?.device?.address != this.gatt!!.device.address) return
        _servicesDiscovered.value = StatusResult(gatt, status)
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        if (this.gatt?.device?.address == null || gatt?.device?.address != this.gatt!!.device.address) return
        _mtuChanged.value = MTUResult(gatt, mtu, status)
    }

    @FlowPreview
    @Throws(SecurityException::class)
    suspend fun connectGatt(context: Context, auto: Boolean, unbondOnTimeout: Boolean = true): BlueGATTConnection? {
        var res: ConnectionStateResult? = null
        try {
            coroutineScope {
                launch(ioDispatcher) {
                    gatt = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            device.connectGatt(context, auto, this@BlueGATTConnection, BluetoothDevice.TRANSPORT_LE, BluetoothDevice.PHY_LE_1M)
                        } else {
                            device.connectGatt(context, auto, this@BlueGATTConnection, BluetoothDevice.TRANSPORT_LE)
                        }
                    } else {
                        device.connectGatt(context, auto, this@BlueGATTConnection)
                    }
                }
                withTimeout(cbTimeout) {
                    res = connectionStateChanged.first()
                }
            }
        } catch (e: TimeoutCancellationException) {
            if (unbondOnTimeout) {
                Timber.w("Gatt timed out. Removing bond and retrying.")

                if (device.bondState == BluetoothDevice.BOND_BONDED) {
                    device::class.java.getMethod("removeBond").invoke(device)
                }

                return connectGatt(context, auto, unbondOnTimeout = false)
            }


            Timber.e("connectGatt timed out")
        }
        if (res?.status != null && res!!.status != BluetoothGatt.GATT_SUCCESS) {
            Timber.e("connectGatt status ${res?.status}")
        }
        return if (res?.isSuccess() == true && res?.newState == BluetoothGatt.STATE_CONNECTED) {
            this
        } else {
            close()
            null
        }
    }

    @Throws(SecurityException::class)
    fun close() {
        gatt?.disconnect()
        gatt?.close()
    }

    @Throws(SecurityException::class)
    suspend fun requestMtu(mtu: Int): MTUResult? {
        gatt!!.requestMtu(mtu)
        var mtuResult: MTUResult? = null
        try {
            withTimeout(cbTimeout) {
                mtuResult = mtuChanged.first()
            }
        } catch (e: TimeoutCancellationException) {
            Timber.e("requestMtu timed out")
        }
        return mtuResult
    }

    @Throws(SecurityException::class)
    suspend fun discoverServices(): StatusResult? {
        if (!gatt!!.discoverServices()) return null
        var result: StatusResult? = null
        try {
            withTimeout(cbTimeout) {
                result = servicesDiscovered.first()
            }
        } catch (e: TimeoutCancellationException) {
            Timber.e("discoverServices timed out")
        }
        return result
    }

    fun getService(uuid: UUID): BluetoothGattService? = gatt!!.getService(uuid)
    @Throws(SecurityException::class)
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enable: Boolean) = gatt!!.setCharacteristicNotification(characteristic, enable)

    @Throws(SecurityException::class)
    suspend fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray): CharacteristicResult? {
        characteristic.value = value
        if (!gatt!!.writeCharacteristic(characteristic)) return null
        var result: CharacteristicResult? = null
        try {
            withTimeout(cbTimeout) {
                result = characteristicWritten.first()
            }
        } catch (e: TimeoutCancellationException) {
            Timber.e("writeCharacteristic timed out")
        }
        return result
    }

    @Throws(SecurityException::class)
    suspend fun readCharacteristic(characteristic: BluetoothGattCharacteristic): CharacteristicResult? {
        if (!gatt!!.readCharacteristic(characteristic)) return null
        var result: CharacteristicResult? = null
        try {
            withTimeout(cbTimeout) {
                result = characteristicRead.first()
            }
        } catch (e: TimeoutCancellationException) {
            Timber.e("readCharacteristic timed out")
        }
        return result
    }

    @Throws(SecurityException::class)
    suspend fun writeDescriptor(descriptor: BluetoothGattDescriptor, value: ByteArray): DescriptorResult? {
        descriptor.value = value
        if (!gatt!!.writeDescriptor(descriptor)) return null
        var result: DescriptorResult? = null
        try {
            withTimeout(cbTimeout) {
                result = descriptorWritten.first { a -> a.descriptor?.uuid == descriptor.uuid && a.descriptor?.characteristic?.uuid == descriptor.characteristic.uuid }
            }
        } catch (e: TimeoutCancellationException) {
            Timber.e("writeDescriptor timed out")
        }
        return result
    }

    @Throws(SecurityException::class)
    suspend fun readDescriptor(descriptor: BluetoothGattDescriptor): DescriptorResult? {
        if (!gatt!!.readDescriptor(descriptor)) return null
        var result: DescriptorResult? = null
        try {
            withTimeout(cbTimeout) {
                result = descriptorRead.first()
            }
        } catch (e: TimeoutCancellationException) {
            Timber.e("readDescriptor timed out")
        }
        return result
    }

    @Throws(SecurityException::class)
    suspend fun disconnect() {
        gatt!!.disconnect()
        try {
            withTimeout(cbTimeout) {
                var result: ConnectionStateResult? = null
                while (result == null) {
                    result = connectionStateChanged.first { a -> a.newState == BluetoothGatt.STATE_DISCONNECTED }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w("disconnect timed out")
        }
    }
}