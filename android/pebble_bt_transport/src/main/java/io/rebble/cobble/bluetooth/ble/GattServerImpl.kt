package io.rebble.cobble.bluetooth.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import timber.log.Timber

class GattServerImpl(private val bluetoothManager: BluetoothManager, private val context: Context, private val services: List<GattService>): GattServer {
    private val scope = CoroutineScope(Dispatchers.Default)
    class GattServerException(message: String) : Exception(message)

    @SuppressLint("MissingPermission")
    val serverFlow: SharedFlow<ServerEvent> = openServer().shareIn(scope, SharingStarted.Lazily, replay = 1)

    private var server: BluetoothGattServer? = null

    override fun getServer(): BluetoothGattServer? {
        return server
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private fun openServer() = callbackFlow {
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

            override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?) {
                if (!listeningEnabled) {
                    Timber.w("Event received while listening disabled: onDescriptorReadRequest")
                    return
                }
                trySend(DescriptorReadEvent(device!!, requestId, offset, descriptor!!) { data ->
                    try {
                        openServer?.sendResponse(device, requestId, data.status, data.offset, data.value)
                    } catch (e: SecurityException) {
                        throw IllegalStateException("No permission to send response", e)
                    }
                })
            }

            override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
                if (!listeningEnabled) {
                    Timber.w("Event received while listening disabled: onDescriptorWriteRequest")
                    return
                }
                trySend(DescriptorWriteEvent(device!!, requestId, descriptor!!, offset, value ?: byteArrayOf()) { status ->
                    try {
                        openServer?.sendResponse(device, requestId, status, offset, null)
                    } catch (e: SecurityException) {
                        throw IllegalStateException("No permission to send response", e)
                    }
                })
            }

            override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
                if (!listeningEnabled) {
                    Timber.w("Event received while listening disabled: onNotificationSent")
                    return
                }
                trySend(NotificationSentEvent(device!!, status))
            }

            override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
                if (!listeningEnabled) {
                    Timber.w("Event received while listening disabled: onMtuChanged")
                    return
                }
                trySend(MtuChangedEvent(device!!, mtu))
            }

            override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                serviceAddedChannel.trySend(ServiceAddedEvent(status, service))
            }
        }
        openServer = bluetoothManager.openGattServer(context, callbacks)
        services.forEach {
            check(serviceAddedChannel.isEmpty) { "Service added event not consumed" }
            val service = it.register(serverFlow)
            if (!openServer.addService(service)) {
                throw GattServerException("Failed to request add service")
            }
            if (serviceAddedChannel.receive().status != BluetoothGatt.GATT_SUCCESS) {
                throw GattServerException("Failed to add service")
            }
        }
        send(ServerInitializedEvent(this@GattServerImpl))
        listeningEnabled = true
        awaitClose { openServer.close() }
    }

    private val serverActor = scope.actor<ServerAction> {
        @SuppressLint("MissingPermission")
        for (action in channel) {
            when (action) {
                is ServerAction.NotifyCharacteristicChanged -> {
                    val device = action.device
                    val characteristic = action.characteristic
                    val confirm = action.confirm
                    val value = action.value
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        server?.notifyCharacteristicChanged(device, characteristic, confirm, value)
                    } else {
                        characteristic.value = value
                        server?.notifyCharacteristicChanged(device, characteristic, confirm)
                    }
                }
            }
        }
    }

    override suspend fun notifyCharacteristicChanged(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic, confirm: Boolean, value: ByteArray) {
        serverActor.send(ServerAction.NotifyCharacteristicChanged(device, characteristic, confirm, value))
    }

    open class ServerAction {
        class NotifyCharacteristicChanged(val device: BluetoothDevice, val characteristic: BluetoothGattCharacteristic, val confirm: Boolean, val value: ByteArray) : ServerAction()
    }

    override fun getFlow(): Flow<ServerEvent> {
        return serverFlow
    }
}