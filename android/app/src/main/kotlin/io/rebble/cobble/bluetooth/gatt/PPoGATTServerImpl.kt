package io.rebble.cobble.bluetooth.gatt

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import androidx.annotation.RequiresPermission
import io.rebble.libpebblecommon.ble.GATTPacket
import io.rebble.libpebblecommon.ble.LEConstants
import io.rebble.cobble.bluetooth.gatt.GATTServer.GATTEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.IOException
import java.util.*

@SuppressLint("MissingPermission")
class PPoGATTServerImpl (
        private val context: Context,
        private val gattServerScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : PPoGATTServer {
    private var targetDevice: BluetoothDevice? = null
     val gattServer = GATTServer(context, gattServerScope)

    private lateinit var dataCharacteristic: BluetoothGattCharacteristic

    var connected = false

    override val currentMtu = gattServer.mtuChangedFlow.filter {
        it.device.address == targetDevice?.address
    }.map { it.mtu }

    private val _packetRxFlow = MutableSharedFlow<GATTPacket>()
    override val packetsFromWatch: SharedFlow<GATTPacket> = _packetRxFlow

    init {
        gattServerScope.launch {
            gattServer.characteristicReadRequestFlow.filter {
                it.device.address == targetDevice?.address
                        && it.characteristic.uuid == UUID.fromString(LEConstants.UUIDs.META_CHARACTERISTIC_SERVER)
            }.collect {
                Timber.d("Meta read")
                onMetaRead(it)
            }
        }

        gattServerScope.launch {
            gattServer.characteristicSubscriptionRequestFlow.filter {
                it.device.address == targetDevice?.address
                        && it.characteristic.uuid == UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER)
            }.collect {
                onDataSubscribed(it)
            }
        }

        gattServerScope.launch {
            gattServer.characteristicWriteRequestFlow.filter {
                it.device.address == targetDevice?.address
                        && it.characteristic.uuid == UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER)
            }.collect{
                onWrite(it)
            }
        }

        packetsFromWatch.onCompletion {
            Timber.d("PacketRXFlow completion")
        }

    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    fun setTarget(device: BluetoothDevice?) {
        if (targetDevice != null && device?.address != targetDevice?.address) {
            gattServer.cancelConnection(targetDevice!!)
        }
        targetDevice = device
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    suspend fun init(): Boolean {
        setTarget(null)
        return gattServer.initServer() && initService()
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    suspend fun initService(): Boolean {
        val gattService = BluetoothGattService(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER), BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val metaCharacteristic = BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.META_CHARACTERISTIC_SERVER), BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED)
        gattService.addCharacteristic(metaCharacteristic)
        dataCharacteristic = BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER), BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE or BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED)
        dataCharacteristic.addDescriptor(BluetoothGattDescriptor(UUID.fromString(LEConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR), BluetoothGattDescriptor.PERMISSION_WRITE))
        gattService.addCharacteristic(dataCharacteristic)

        val padService = BluetoothGattService(UUID.fromString(LEConstants.UUIDs.FAKE_SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY)
        padService.addCharacteristic(BluetoothGattCharacteristic(UUID.fromString(LEConstants.UUIDs.FAKE_SERVICE_UUID), BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ))

        if (gattServer.getService(UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER)) != null) {
            Timber.w("Service already registered, clearing services and then re-registering")
            gattServer.clearServices()
        }
        return if (gattServer.addService(gattService) && gattServer.addService(padService)) {
            Timber.d("Server set up and ready for connection")
            true
        } else {
            Timber.e("Failed to add service")
            false
        }
    }

    override suspend fun sendToWatch(packet: GATTPacket) {
        val data = packet.data

        try {
            Timber.d("WRITE")
            val res = withTimeout(2000) {
                return@withTimeout gattServer.notifyCharacteristic(targetDevice!!, dataCharacteristic, data, false)
            }
            if (!res) {
                throw IOException("Failed to notify characteristic")
            }
        } catch (e: TimeoutCancellationException) {
            throw IOException("Timed out notifying characteristic", e)
        }
    }

    /* Events */
    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    suspend fun onMetaRead(request: GATTEvent.CharacteristicReadRequest) {
        if (!gattServer.sendResponse(request, LEConstants.SERVER_META_RESPONSE)) {
            Timber.e("Error sending meta response to device")
            closePebble()
        } else {
            delay(5000)
            if (!connected) {
                Timber.e("No data from watch after 5s")
                closePebble()
            }
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    suspend fun onDataSubscribed(request: GATTEvent.CharacteristicSubscriptionRequest) {
        if (request.notify) {
            Timber.d("Data subscribed")
        } else {
            Timber.e("Data unsubscribed on connection")
            closePebble()
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    suspend fun onWrite(request: GATTEvent.CharacteristicWriteRequest) {
        if (request.value == null || request.value.isEmpty()) {
            Timber.w("Ignoring empty write to device characteristic")
            return
        }
        connected = true
        val data = request.value
        val packet = GATTPacket(data)

        try {
            withTimeout(5000) {
                _packetRxFlow.emit(packet)
            }
        } catch (e: TimeoutCancellationException) {
            throw Error("Took too long to ingest packet (seq ${packet.sequence}), see cause", e)
        }
    }

    fun closePebble() {
        setTarget(null)
    }
}