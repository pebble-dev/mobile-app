package io.rebble.cobble.bluetooth.gatt

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import io.rebble.libpebblecommon.ble.LEConstants
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.UUID
import kotlin.experimental.and

class GATTServer(private val context: Context, private val serverScope: CoroutineScope = CoroutineScope(Dispatchers.IO)) : BluetoothGattServerCallback() {
    private lateinit var bluetoothGattServer: BluetoothGattServer

    private val _addedServiceFlow = MutableSharedFlow<GATTEvent.AddedServiceResult>()
    private val _mtuChangedFlow = MutableSharedFlow<GATTEvent.MtuChanged>()
    private val _notificationSentFlow = MutableSharedFlow<GATTEvent.NotificationSent>()
    private val _connectionStateChangedFlow = MutableSharedFlow<GATTEvent.ConnectionStateChanged>()
    private val _descriptorWriteRequestFlow = MutableSharedFlow<GATTEvent.DescriptorWriteRequest>()
    private val _characteristicWriteRequestFlow = MutableSharedFlow<GATTEvent.CharacteristicWriteRequest>()
    private val _characteristicReadRequestFlow = MutableSharedFlow<GATTEvent.CharacteristicReadRequest>()
    private val _characteristicSubscriptionRequestFlow = MutableSharedFlow<GATTEvent.CharacteristicSubscriptionRequest>()

    val addedServiceFlow: SharedFlow<GATTEvent.AddedServiceResult> = _addedServiceFlow
    val mtuChangedFlow: SharedFlow<GATTEvent.MtuChanged> = _mtuChangedFlow
    val notificationSentFlow: SharedFlow<GATTEvent.NotificationSent> = _notificationSentFlow
    val connectionStateChangedFlow: SharedFlow<GATTEvent.ConnectionStateChanged> = _connectionStateChangedFlow
    val descriptorWriteRequestFlow: SharedFlow<GATTEvent.DescriptorWriteRequest> = _descriptorWriteRequestFlow
    val characteristicWriteRequestFlow: SharedFlow<GATTEvent.CharacteristicWriteRequest> = _characteristicWriteRequestFlow
    val characteristicReadRequestFlow: SharedFlow<GATTEvent.CharacteristicReadRequest> = _characteristicReadRequestFlow
    val characteristicSubscriptionRequestFlow: SharedFlow<GATTEvent.CharacteristicSubscriptionRequest> = _characteristicSubscriptionRequestFlow

    private var inited = false

    sealed class GATTEvent {
        class CharacteristicWriteRequest(
                val device: BluetoothDevice,
                val requestId: Int,
                val characteristic: BluetoothGattCharacteristic,
                val preparedWrite: Boolean,
                val responseNeeded: Boolean,
                val offset: Int,
                val value: ByteArray?
        ) : GATTEvent()

        class CharacteristicReadRequest(
                val device: BluetoothDevice,
                val requestId: Int,
                val characteristic: BluetoothGattCharacteristic,
                val offset: Int
        ) : GATTEvent()

        class DescriptorWriteRequest(
                val device: BluetoothDevice,
                val requestId: Int,
                val descriptor: BluetoothGattDescriptor,
                val preparedWrite: Boolean,
                val responseNeeded: Boolean,
                val offset: Int,
                val value: ByteArray?
        ) : GATTEvent()

        class CharacteristicSubscriptionRequest(
                val device: BluetoothDevice,
                val characteristic: BluetoothGattCharacteristic,
                val notify: Boolean
        ) : GATTEvent()

        class NotificationSent(
                val device: BluetoothDevice,
                val status: Int
        ) : GATTEvent()

        class AddedServiceResult(
                val status: Int,
                val service: BluetoothGattService?
        ) : GATTEvent()

        class MtuChanged(
                val device: BluetoothDevice,
                val mtu: Int
        ) : GATTEvent()

        class ConnectionStateChanged(
                device: BluetoothDevice,
                status: Int,
                newState: Int
        ) : GATTEvent()
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    public fun initServer(): Boolean {
        if (inited) return true

        val bluetoothManager = context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothGattServer = bluetoothManager.openGattServer(context, this)!!
        inited = true
        return true
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    public fun clearServices() {
        bluetoothGattServer.clearServices()
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
       serverScope.launch {
           _addedServiceFlow.emit(GATTEvent.AddedServiceResult(status, service))
       }
    }

    override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
        Timber.d("onCharacteristicWrite")
        device?.let { _device ->
            characteristic?.let { _characteristic ->
                serverScope.launch {
                    _characteristicWriteRequestFlow.emit(
                            GATTEvent.CharacteristicWriteRequest(
                                    _device,
                                    requestId,
                                    _characteristic,
                                    preparedWrite,
                                    responseNeeded,
                                    offset,
                                    value
                            )
                    )
                }
            }
        }
    }

    override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
        Timber.d("onCharacteristicRead")
        device?.let { _device ->
            characteristic?.let { _characteristic ->
                serverScope.launch {
                    _characteristicReadRequestFlow.emit(
                            GATTEvent.CharacteristicReadRequest(
                                    _device,
                                    requestId,
                                    _characteristic,
                                    offset
                            )
                    )
                }
            }
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
        device?.let { _device ->
            descriptor?.let { _descriptor ->
                serverScope.launch {
                    if (descriptor.uuid == UUID.fromString(LEConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR) && value != null) {
                        Timber.d("Subscription change")
                        if (!responseNeeded || bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value)) {
                            _characteristicSubscriptionRequestFlow.emit(
                                    GATTEvent.CharacteristicSubscriptionRequest(
                                            _device,
                                            _descriptor.characteristic,
                                            (value[0] and 1) == 1.toByte()
                                    )
                            )
                        }
                    } else {
                        Timber.d("onDescriptorWrite")
                        _descriptorWriteRequestFlow.emit(
                                GATTEvent.DescriptorWriteRequest(
                                        _device,
                                        requestId,
                                        _descriptor,
                                        preparedWrite,
                                        responseNeeded,
                                        offset,
                                        value
                                )
                        )
                    }
                }
            }
        }
    }

    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
        device?.let { _device ->
            serverScope.launch {
                _connectionStateChangedFlow.emit(GATTEvent.ConnectionStateChanged(_device, status, newState))
            }
        }
    }

    override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
        Timber.d("onNotificationSent")
        device?.let { _device ->
            serverScope.launch {
                _notificationSentFlow.emit(GATTEvent.NotificationSent(_device, status))
            }
        }
    }

    override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
        device?.let { _device ->
            serverScope.launch {
                _mtuChangedFlow.emit(GATTEvent.MtuChanged(_device, mtu))
            }
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    public suspend fun notifyCharacteristic(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic, value: ByteArray?, confirm: Boolean): Boolean {
        characteristic.value = value
        if (!bluetoothGattServer.notifyCharacteristicChanged(device, characteristic, confirm)) {
            return false
        }
        val result = notificationSentFlow.first { it.device.address == device.address }
        return result.status == BluetoothGatt.GATT_SUCCESS
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    public suspend fun sendResponse(request: GATTEvent.CharacteristicReadRequest, responseData: ByteArray?): Boolean {
        return bluetoothGattServer.sendResponse(request.device, request.requestId, BluetoothGatt.GATT_SUCCESS, request.offset, responseData)
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    public fun cancelConnection(device: BluetoothDevice) {
        bluetoothGattServer.cancelConnection(device)
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    public suspend fun addService(service: BluetoothGattService): Boolean {
        bluetoothGattServer.addService(service)
        try {
            return withTimeout(5000) {
                return@withTimeout addedServiceFlow.first {it.service?.uuid == service.uuid }
            }.status == BluetoothGatt.GATT_SUCCESS
        } catch (e: TimeoutCancellationException) {
            return false
        }
    }

    public fun getService(uuid: UUID): BluetoothGattService? = bluetoothGattServer.getService(uuid)
}