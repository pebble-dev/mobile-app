package io.rebble.cobble.transport.bluetooth

import android.bluetooth.BluetoothGattCharacteristic
import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber
import kotlin.experimental.and
import kotlin.properties.Delegates

/**
 * Talks to watch connectivity characteristic describing pair status, connection, and other parameters
 */
class ConnectivityWatcher(val gatt: BlueGATTConnection) {
    private var isSubscribed = false

    @ExperimentalUnsignedTypes
    class ConnectivityStatus(characteristicValue: ByteArray) {
        var connected by Delegates.notNull<Boolean>()
        var paired by Delegates.notNull<Boolean>()
        var encrypted by Delegates.notNull<Boolean>()
        var hasBondedGateway by Delegates.notNull<Boolean>()
        var supportsPinningWithoutSlaveSecurity by Delegates.notNull<Boolean>()
        var hasRemoteAttemptedToUseStalePairing by Delegates.notNull<Boolean>()
        var pairingErrorCode: PairingErrorCode

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

        override fun toString(): String = "< ConnectivityStatus connected = ${connected} paired = ${paired} encrypted = ${encrypted} hasBondedGateway = ${hasBondedGateway} supportsPinningWithoutSlaveSecurity = ${supportsPinningWithoutSlaveSecurity} hasRemoteAttemptedToUseStalePairing = ${hasRemoteAttemptedToUseStalePairing} pairingErrorCode = ${pairingErrorCode}>"
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
                return v ?: UNKNOWN_ERROR
            }
        }
    }

    var connectivityStatus = CompletableDeferred<ConnectivityStatus>()

    suspend fun subscribe(): Boolean {
        val pairService = gatt.getService(BlueGATTConstants.UUIDs.PAIRING_SERVICE_UUID)
        if (pairService == null) {
            Timber.e("pairService is null")
            return false
        } else {
            val connectivityCharacteristic = pairService.getCharacteristic(BlueGATTConstants.UUIDs.CONNECTIVITY_CHARACTERISTIC)
            if (connectivityCharacteristic == null) {
                Timber.e("connectivityCharacteristic is null")
            } else {
                val configDescriptor = connectivityCharacteristic.getDescriptor(BlueGATTConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR)
                if (configDescriptor == null) {
                    Timber.e("configDescriptor for connectivityCharacteristic is null")
                    return false
                } else {
                    Timber.d("Requesting subscribe to connectivity characteristic")
                    if (!gatt.setCharacteristicNotification(connectivityCharacteristic, true)) {
                        Timber.e("BluetoothGatt refused to subscribe to connectivity characteristic")
                    } else {
                        if (gatt.writeDescriptor(configDescriptor, BlueGATTConstants.CHARACTERISTIC_SUBSCRIBE_VALUE)?.isSuccess() != true) {
                            Timber.e("Failed to write subscribe value to connectivityCharacteristic's configDescriptor")
                        } else {
                            isSubscribed = true
                            Timber.d("Subscribed successfully")
                            return true
                        }
                    }

                }
            }
        }
        return false
    }

    fun onCharacteristicChanged(characteristic: BluetoothGattCharacteristic?) {
        if (characteristic?.uuid == BlueGATTConstants.UUIDs.CONNECTIVITY_CHARACTERISTIC) {
            if (characteristic != null) {
                val status = ConnectivityStatus(characteristic.value)
                //GlobalScope.launch(Dispatchers.IO) { onConnectivityChanged(lastStatus!!) }
                Timber.d(status.toString())
                connectivityStatus.complete(status)
            }
        }
    }

    suspend fun getStatus(): ConnectivityStatus {
        try {
            return connectivityStatus.await()
        } finally {
            connectivityStatus = CompletableDeferred()
        }
    }
}