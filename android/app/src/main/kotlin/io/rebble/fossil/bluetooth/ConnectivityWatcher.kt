package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import io.rebble.fossil.util.toHexString
import kotlin.experimental.and
import kotlin.properties.Delegates

class ConnectivityWatcher(val gatt: BluetoothGatt, val onConnectivityChanged: (ConnectivityStatus) -> Unit) {
    private val logTag = "ConnectivityWatcher"
    private var isSubscribed = false
    var lastStatus: ConnectivityStatus? = null

    @ExperimentalUnsignedTypes
    class ConnectivityStatus(characteristicValue: ByteArray) {
        var connected by Delegates.notNull<Boolean>()
        var paired by Delegates.notNull<Boolean>()
        var encrypted by Delegates.notNull<Boolean>()
        var hasBondedGateway by Delegates.notNull<Boolean>()
        var supportsPinningWithoutSlaveSecurity by Delegates.notNull<Boolean>()
        var hasRemoteAttemptedToUseStalePairing by Delegates.notNull<Boolean>()
        lateinit var pairingErrorCode: PairingErrorCode

        init {
            val flags = characteristicValue[0]
            connected = flags and 0b1 > 0
            paired = flags and 0b10 > 0
            encrypted = flags and 0b100 > 0
            hasBondedGateway = flags and 0b1000 > 0
            supportsPinningWithoutSlaveSecurity = flags and 0b10000 > 0
            hasRemoteAttemptedToUseStalePairing = flags and 0b100000 > 0
            pairingErrorCode = PairingErrorCode.getByValue(characteristicValue[3])
        }
    }

    enum class PairingErrorCode(val value: Byte) {
        NO_ERROR(0),
        PASSKEY_ENTRY_FAILED(1),
        OOB_NOT_AVAILABLE(2),
        AUTHENTICATION_REQUIREMENTS(3),
        CONFIRM_VALUE_FAILED(4),
        PAIRING_NOT_SUPPORTED(5),
        ENCRYPTION_KEY_SIZE(6),
        COMMAND_NOT_SUPPORTED(7),
        UNSPECIFIED_REASON(8),
        REPEATED_ATTEMPTS(9),
        INVALID_PARAMETERS(10),
        DHKEY_CHECK_FAILED(11),
        NUMERIC_COMPARISON_FAILED(12),
        BR_EDR_PAIRING_IN_PROGRESS(13),
        CROSS_TRANSPORT_KEY_DERIVATION_NOT_ALLOWED(14),
        UNKNOWN_ERROR(255u.toByte());

        companion object {
            fun getByValue(value: Byte): PairingErrorCode {
                val v = values().firstOrNull { it.value == value }
                return if (v == null) UNKNOWN_ERROR else v
            }
        }
    }

    fun subscribe(): Boolean {
        val pairService = gatt.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)
        if (pairService == null) {
            Log.e(logTag, "pairService is null")
            return false
        } else {
            val connectivityCharacteristic = pairService.getCharacteristic(BlueGATTConstants.UUIDs.CONNECTIVITY_CHARACTERISTIC)
            if (connectivityCharacteristic == null) {
                Log.e(logTag, "connectivityCharacteristic is null")
            } else {
                val configDescriptor = connectivityCharacteristic.getDescriptor(BlueGATTConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR)
                if (configDescriptor == null) {
                    Log.e(logTag, "configDescriptor for connectivityCharacteristic is null")
                    return false
                } else {
                    configDescriptor.setValue(BlueGATTConstants.CHARACTERISTIC_SUBSCRIBE_VALUE)
                    if (!gatt.writeDescriptor(configDescriptor)) {
                        Log.e(logTag, "Failed to write subscribe value to connectivityCharacteristic's configDescriptor")
                        return false
                    } else {
                        Log.d(logTag, "Requesting subscribe to connectivity characteristic")
                        if (!gatt.setCharacteristicNotification(connectivityCharacteristic, true)) {
                            Log.e(logTag, "BluetoothGatt refused to subscribe to connectivity characteristic")
                            return false
                        }
                    }
                }
            }
        }
        return true
    }

    fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        if (descriptor?.characteristic?.uuid == BlueGATTConstants.UUIDs.CONNECTIVITY_CHARACTERISTIC && descriptor?.uuid == BlueGATTConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(logTag, "Subscribed to connectivity characteristic")
                isSubscribed = true
                val currentConnectivity = descriptor?.characteristic?.value
                if (currentConnectivity == null) {
                    Log.d(logTag, "Connectivity descriptor value null")
                } else {
                    lastStatus = ConnectivityStatus(currentConnectivity)
                    onConnectivityChanged(lastStatus!!)
                }
            } else {
                Log.e(logTag, "Subscribe to connectivity characteristic failed")
                isSubscribed = false
            }
        }
    }

    fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        if (characteristic?.uuid == BlueGATTConstants.UUIDs.CONNECTIVITY_CHARACTERISTIC) {
            if (isSubscribed && characteristic != null) {
                lastStatus = ConnectivityStatus(characteristic.value)
                onConnectivityChanged(lastStatus!!)
            }
        }
    }
}