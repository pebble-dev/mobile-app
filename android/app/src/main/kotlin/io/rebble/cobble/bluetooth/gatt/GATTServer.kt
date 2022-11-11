package io.rebble.cobble.bluetooth.gatt

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import io.rebble.libpebblecommon.ble.LEConstants
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.util.UUID
import kotlin.experimental.and

open class GATTServer(private val context: Context) : BluetoothGattServerCallback() {
    protected lateinit var bluetoothGattServer: BluetoothGattServer
    protected var targetDevice: BluetoothDevice? = null
    protected val characteristicReadHandlers = mutableMapOf<UUID, (CharacteristicReadRequest) -> Unit>()
    protected val characteristicSubscriptionHandlers = mutableMapOf<UUID, (CharacteristicSubscriptionRequest) -> Unit>()
    protected val characteristicWriteHandlers = mutableMapOf<UUID, (CharacteristicWriteRequest) -> Unit>()

    private fun checkTarget(requestAddress: String): Boolean = targetDevice?.address != null && targetDevice?.address == requestAddress

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    protected fun initServer(): Boolean {
        val bluetoothManager = context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothGattServer = bluetoothManager.openGattServer(context, this)!!
        return true
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    protected fun clearServices() {
        bluetoothGattServer.clearServices()
    }

    private inner class AddedServiceResult(
            val status: Int,
            val service: BluetoothGattService?
    )

    private val addedServiceChannel = Channel<AddedServiceResult>(Channel.UNLIMITED)
    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        addedServiceChannel.trySend(AddedServiceResult(status, service))
    }

    protected inner class CharacteristicWriteRequest(
            val device: BluetoothDevice,
            val requestId: Int,
            val characteristic: BluetoothGattCharacteristic,
            val preparedWrite: Boolean,
            val responseNeeded: Boolean,
            val offset: Int,
            val value: ByteArray?
    )

    override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
        Timber.d("onCharacteristicWrite")
        if (device != null && characteristic != null && checkTarget(device.address)) {
            characteristicWriteHandlers[characteristic.uuid]?.invoke(
                    CharacteristicWriteRequest(
                            device,
                            requestId,
                            characteristic,
                            preparedWrite,
                            responseNeeded,
                            offset,
                            value
                    )
            )
        }
    }

    protected inner class CharacteristicReadRequest(
            val device: BluetoothDevice,
            val requestId: Int,
            val characteristic: BluetoothGattCharacteristic,
            val offset: Int
    )
    override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
        Timber.d("onCharacteristicRead: ${checkTarget(device!!.address)}")
        if (device != null && characteristic != null && checkTarget(device.address)) {
            characteristicReadHandlers[characteristic.uuid]?.invoke(
                    CharacteristicReadRequest(
                            device,
                            requestId,
                            characteristic,
                            offset
                    )
            )
        }
    }

    protected inner class DescriptorWriteRequest(
            val device: BluetoothDevice,
            val requestId: Int,
            val descriptor: BluetoothGattDescriptor,
            val preparedWrite: Boolean,
            val responseNeeded: Boolean,
            val offset: Int,
            val value: ByteArray?
    )

    protected inner class CharacteristicSubscriptionRequest(
            val device: BluetoothDevice,
            val characteristic: BluetoothGattCharacteristic,
            val notify: Boolean
    )
    protected val descriptorWriteRequestChannel = Channel<DescriptorWriteRequest>(Channel.UNLIMITED)
    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
        Timber.d("onDescriptorWrite")
        if (device != null && descriptor != null && checkTarget(device.address)) {
            if (descriptor.uuid == UUID.fromString(LEConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR) && value != null) {
                if (!responseNeeded || bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value)) {
                    characteristicSubscriptionHandlers[descriptor.characteristic.uuid]?.invoke(
                            CharacteristicSubscriptionRequest(
                                    device,
                                    descriptor.characteristic,
                                    (value[0] and 1) == 1.toByte()
                            )
                    )
                }
            } else {
                descriptorWriteRequestChannel.trySend(
                        DescriptorWriteRequest(
                                device,
                                requestId,
                                descriptor,
                                preparedWrite,
                                responseNeeded,
                                offset,
                                value
                        )
                )
            }
        }
    }

    protected inner class NotificationSent(
            val device: BluetoothDevice,
            val status: Int
    )
    protected val notificationSentChannel = Channel<NotificationSent>(Channel.UNLIMITED)
    override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
        Timber.d("onNotificationSent")
        if (device != null && checkTarget(device.address)) {
            notificationSentChannel.trySend(NotificationSent(device, status))
        }
    }

    protected suspend fun addService(service: BluetoothGattService): Boolean {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        bluetoothGattServer.addService(service)
        return withTimeout(1000) {
            return@withTimeout addedServiceChannel.receive()
        }.status == BluetoothGatt.GATT_SUCCESS
    }

    protected fun getService(uuid: UUID): BluetoothGattService? = bluetoothGattServer.getService(uuid)
}