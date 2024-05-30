package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.IntentSender
import android.os.ParcelUuid
import androidx.annotation.RequiresPermission
import io.rebble.cobble.bluetooth.getBluetoothDevicePairEvents
import io.rebble.libpebblecommon.ble.LEConstants
import io.rebble.libpebblecommon.packets.PhoneAppVersion
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.io.IOException
import java.util.BitSet
import java.util.UUID
import java.util.concurrent.Executor
import java.util.regex.Pattern

@OptIn(ExperimentalUnsignedTypes::class)
class PebbleLEConnector(private val connection: BlueGATTConnection, private val context: Context, private val scope: CoroutineScope) {
    companion object {
        private val PENDING_BOND_TIMEOUT = 30000L // Requires user interaction, so needs a longer timeout
        private val CONNECTIVITY_UPDATE_TIMEOUT = 10000L
    }

    enum class ConnectorState {
        CONNECTING,
        PAIRING,
        CONNECTED
    }
    @Throws(IOException::class, SecurityException::class)
    suspend fun connect() = flow {
        var success = connection.discoverServices()?.isSuccess() == true
        if (!success) {
            throw IOException("Failed to discover services")
        }
        emit(ConnectorState.CONNECTING)
        success = connection.requestMtu(LEConstants.TARGET_MTU)?.isSuccess() == true
        if (!success) {
            throw IOException("Failed to request MTU")
        }
        val paramManager = ConnectionParamManager(connection)
        success = paramManager.subscribe()
        if (!success) {
            Timber.w("Continuing without connection parameters management")
        }
        val connectivityWatcher = ConnectivityWatcher(connection)
        success = connectivityWatcher.subscribe()
        if (!success) {
            throw IOException("Failed to subscribe to connectivity changes")
        } else {
            Timber.d("Subscribed to connectivity changes")
        }
        val connectionStatus = withTimeout(CONNECTIVITY_UPDATE_TIMEOUT) {
            connectivityWatcher.getStatusFlowed()
        }
        Timber.d("Connection status: $connectionStatus")
        if (connectionStatus.paired) {
            if (connection.device.bondState == BluetoothDevice.BOND_BONDED) {
                Timber.d("Device already bonded. Waiting for watch connection")
                if (connectionStatus.connected) {
                    emit(ConnectorState.CONNECTED)
                    return@flow
                } else {
                    val nwConnectionStatus = connectivityWatcher.getStatusFlowed()
                    check(nwConnectionStatus.connected) { "Failed to connect to watch" }
                    emit(ConnectorState.CONNECTED)
                    return@flow
                }
            } else {
                Timber.d("Watch is paired but phone is not")
                emit(ConnectorState.PAIRING)
                requestPairing(connectionStatus)
            }
        } else {
            if (connection.device.bondState == BluetoothDevice.BOND_BONDED) {
                Timber.w("Phone is bonded but watch is not paired")
                //TODO: Request user to remove bond
                emit(ConnectorState.PAIRING)
                requestPairing(connectionStatus)
            } else {
                Timber.d("Not paired")
                emit(ConnectorState.PAIRING)
                requestPairing(connectionStatus)
            }
        }
        emit(ConnectorState.CONNECTED)
    }

    private fun getBondStateFlow() = getBluetoothDevicePairEvents(context, connection.device.address)

    @Throws(IOException::class, SecurityException::class)
    private suspend fun requestPairing(connectivityRecord: ConnectivityWatcher.ConnectivityStatus) {
        Timber.d("Requesting pairing")
        val pairingService = connection.getService(UUID.fromString(LEConstants.UUIDs.PAIRING_SERVICE_UUID))
        check(pairingService != null) { "Pairing service not found" }
        val pairingTriggerCharacteristic = pairingService.getCharacteristic(UUID.fromString(LEConstants.UUIDs.PAIRING_TRIGGER_CHARACTERISTIC))
        check(pairingTriggerCharacteristic != null) { "Pairing trigger characteristic not found" }

        val bondState = getBondStateFlow()
        var needsExplicitBond = true

        val bondBonded = scope.async {
            bondState.first { it.bondState == BluetoothDevice.BOND_BONDED }
        }
        val bondBonding = scope.async {
            bondState.first { it.bondState == BluetoothDevice.BOND_BONDING }
        }

        // A writeable pairing trigger allows addr pinning
        val writeablePairTrigger = pairingTriggerCharacteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0
        if (writeablePairTrigger) {
            needsExplicitBond = connectivityRecord.supportsPinningWithoutSlaveSecurity
            val pairValue = makePairingTriggerValue(needsExplicitBond, autoAcceptFuturePairing = false, watchAsGattServer = false)
            if (connection.writeCharacteristic(pairingTriggerCharacteristic, pairValue)?.isSuccess() != true) {
                throw IOException("Failed to request pinning")
            }
        }
        if (needsExplicitBond) {
            Timber.d("Explicit bond required")
            connection.device.createBond()
        }
        withTimeout(PENDING_BOND_TIMEOUT) {
            bondBonding.await()
        }
        Timber.d("Bonding started")
        check(bondBonded.await().bondState == BluetoothDevice.BOND_BONDED) { "Failed to bond, reason = ${bondBonded.await().unbondReason}" }
    }

    private fun makePairingTriggerValue(noSecurityRequest: Boolean, autoAcceptFuturePairing: Boolean, watchAsGattServer: Boolean): ByteArray {
        val value = BitSet(8)
        value[0] = true
        value[1] = noSecurityRequest
        value[2] = true
        value[3] = autoAcceptFuturePairing
        value[4] = watchAsGattServer
        return byteArrayOf(value.toByteArray().first())
    }
}