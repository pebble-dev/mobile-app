package io.rebble.cobble.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import io.rebble.cobble.bluetooth.gatt.GATTServerMessage
import io.rebble.libpebblecommon.ble.LEConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.*

@SuppressLint("MissingPermission")
open class GATTServer(bluetoothManager: BluetoothManager, context: Context, scope: CoroutineScope) {
    private lateinit var gattServer: BluetoothGattServer
    private val characteristics: MutableMap<UUID, BluetoothGattCharacteristic> = mutableMapOf()

    protected val serverMessages: Flow<GATTServerMessage> = callbackFlow<GATTServerMessage> {
        val callbacks = object : BluetoothGattServerCallback() {
            override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
                trySend(GATTServerMessage.CharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value))
            }

            override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
                trySend(GATTServerMessage.CharacteristicReadRequest(device, requestId, offset, characteristic))
            }

            override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
                trySend(GATTServerMessage.DescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value))
            }

            override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?) {
                trySend(GATTServerMessage.DescriptorReadRequest(device, requestId, offset, descriptor))
            }

            override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                trySend(GATTServerMessage.ServiceAdded(status, service))
            }

            override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
                trySend(GATTServerMessage.NotificationSent(device, status))
            }

            override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
                trySend(GATTServerMessage.ConnectionStateChange(device, status, newState))
            }

            override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
                trySend(GATTServerMessage.MtuChanged(device, mtu))
            }
        }
        gattServer = bluetoothManager.openGattServer(context, callbacks)!!
        awaitClose {
            Timber.d("serverMessages closing")
            gattServer.close()
        }
    }.shareIn(scope, SharingStarted.Eagerly).buffer()

    protected fun sendResponse(device: BluetoothDevice, requestId: Int, status: Int, offset: Int, value: ByteArray): Boolean {
        return gattServer.sendResponse(device, requestId, status, offset, value)
    }

    protected fun notifyCharacteristicChanged(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic, confirm: Boolean): Boolean {
        return gattServer.notifyCharacteristicChanged(device, characteristic, confirm)
    }

    protected fun notifyValue(device: BluetoothDevice, value: ByteArray, characteristic: UUID): Boolean {
        val target = characteristics[characteristic]!!
        target.value = value
        return notifyCharacteristicChanged(device, target, false)
    }

    protected suspend fun addService(service: BluetoothGattService): Boolean {
        var res = gattServer.addService(service)
        if (!res) return res

        res = serverMessages
                .filterIsInstance<GATTServerMessage.ServiceAdded>()
                .first { it.service?.uuid == service.uuid }
                .status == BluetoothGatt.GATT_SUCCESS
        return res
    }
}