package io.rebble.cobble.bluetooth.ble

import android.bluetooth.*
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import java.util.UUID

class GattServer(private val bluetoothManager: BluetoothManager, private val context: Context, private val services: List<BluetoothGattService>) {
    class GattServerException(message: String) : Exception(message)
    @OptIn(ExperimentalCoroutinesApi::class)
    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    fun openServer() = callbackFlow {
        var openServer: BluetoothGattServer? = null
        val serviceAddedChannel = Channel<ServiceAddedEvent>(Channel.CONFLATED)
        var listeningEnabled = false
        val callbacks = object : BluetoothGattServerCallback() {
            override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
                if (!listeningEnabled) {
                    Timber.w("Event received while listening disabled: onConnectionStateChange")
                    return
                }
                trySend(ConnectionStateEvent(device, status, newState))
            }
            override fun onCharacteristicReadRequest(device: BluetoothDevice, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic) {
                if (!listeningEnabled) {
                    Timber.w("Event received while listening disabled: onCharacteristicReadRequest")
                    return
                }
                trySend(CharacteristicReadEvent(device, requestId, offset, characteristic) { data ->
                    try {
                        openServer?.sendResponse(device, requestId, data.status, data.offset, data.value)
                    } catch (e: SecurityException) {
                        throw IllegalStateException("No permission to send response", e)
                    }
                })
            }
            override fun onCharacteristicWriteRequest(device: BluetoothDevice, requestId: Int, characteristic: BluetoothGattCharacteristic,
                                                      preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray) {
                if (!listeningEnabled) {
                    Timber.w("Event received while listening disabled: onCharacteristicWriteRequest")
                    return
                }
                trySend(CharacteristicWriteEvent(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value) { status ->
                    try {
                        openServer?.sendResponse(device, requestId, status, offset, null)
                    } catch (e: SecurityException) {
                        throw IllegalStateException("No permission to send response", e)
                    }
                })
            }

            override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                serviceAddedChannel.trySend(ServiceAddedEvent(status, service))
            }
        }
        openServer = bluetoothManager.openGattServer(context, callbacks)
        services.forEach {
            check(serviceAddedChannel.isEmpty) { "Service added event not consumed" }
            if (!openServer.addService(it)) {
                throw GattServerException("Failed to request add service")
            }
            if (serviceAddedChannel.receive().status != BluetoothGatt.GATT_SUCCESS) {
                throw GattServerException("Failed to add service")
            }
        }
        send(ServerInitializedEvent(openServer))
        listeningEnabled = true
        awaitClose { openServer.close() }
    }
}