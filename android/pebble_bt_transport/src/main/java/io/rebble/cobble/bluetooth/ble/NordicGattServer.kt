package io.rebble.cobble.bluetooth.ble

import android.content.Context
import androidx.annotation.RequiresPermission
import io.rebble.libpebblecommon.ble.LEConstants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.server.main.ServerBleGatt
import no.nordicsemi.android.kotlin.ble.server.main.ServerConnectionEvent
import no.nordicsemi.android.kotlin.ble.server.main.data.ServerConnectionOption
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattCharacteristicConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattDescriptorConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.LoggerFactoryFriend
import timber.log.Timber
import java.io.Closeable
import java.util.UUID
import kotlin.coroutines.CoroutineContext

@OptIn(FlowPreview::class)
class NordicGattServer(private val ioDispatcher: CoroutineContext = Dispatchers.IO, private val context: Context): Closeable {
    enum class State {
        INIT,
        OPEN,
        CLOSED
    }
    private val _state = MutableStateFlow(State.INIT)
    val state = _state.asStateFlow()

    private val ppogServiceConfig = ServerBleGattServiceConfig(
            uuid = UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER),
            type = ServerBleGattServiceType.SERVICE_TYPE_PRIMARY,
            characteristicConfigs = listOf(
                    // Meta characteristic
                    ServerBleGattCharacteristicConfig(
                            uuid = UUID.fromString(LEConstants.UUIDs.META_CHARACTERISTIC_SERVER),
                            properties = listOf(
                                    BleGattProperty.PROPERTY_READ,
                            ),
                            permissions = listOf(
                                    BleGattPermission.PERMISSION_READ_ENCRYPTED,
                            ),
                            initialValue = DataByteArray(LEConstants.SERVER_META_RESPONSE)
                    ),
                    // Data characteristic
                    ServerBleGattCharacteristicConfig(
                            uuid = UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER),
                            properties = listOf(
                                    BleGattProperty.PROPERTY_WRITE_NO_RESPONSE,
                                    BleGattProperty.PROPERTY_NOTIFY,
                            ),
                            permissions = listOf(
                                    BleGattPermission.PERMISSION_WRITE_ENCRYPTED,
                            ),
                            descriptorConfigs = listOf(
                                    ServerBleGattDescriptorConfig(
                                            uuid = UUID.fromString(LEConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR),
                                            permissions = listOf(
                                                    BleGattPermission.PERMISSION_WRITE
                                            )
                                    )
                            )
                    )
            )
    )

    private val fakeServiceConfig = ServerBleGattServiceConfig(
            uuid = UUID.fromString(LEConstants.UUIDs.FAKE_SERVICE_UUID),
            type = ServerBleGattServiceType.SERVICE_TYPE_PRIMARY,
            characteristicConfigs = listOf(
                    ServerBleGattCharacteristicConfig(
                            uuid = UUID.fromString(LEConstants.UUIDs.FAKE_SERVICE_UUID),
                            properties = listOf(
                                    BleGattProperty.PROPERTY_READ,
                            ),
                            permissions = listOf(
                                    BleGattPermission.PERMISSION_READ_ENCRYPTED,
                            ),
                    )
            )
    )

    private var scope: CoroutineScope? = null
    private var server: ServerBleGatt? = null
    private val connections: MutableMap<String, PPoGServiceConnection> = mutableMapOf()
    val isOpened: Boolean
        get() = scope?.isActive == true

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    suspend fun open(mockServerDevice: MockServerDevice? = null) {
        Timber.i("Opening GattServer")
        if (scope?.isActive == true) {
            Timber.w("GattServer already open")
            return
        }
        val serverScope = CoroutineScope(ioDispatcher)
        serverScope.coroutineContext.job.invokeOnCompletion {
            Timber.v("GattServer scope closed")
            close()
        }
        server = ServerBleGatt.create(
                context, serverScope,
                ppogServiceConfig,
                fakeServiceConfig,
                mock = mockServerDevice,
                options = ServerConnectionOption(bufferSize = 32)
        ).also { server ->
            server.connectionEvents
                    .debounce(1000)
                    .mapNotNull { it as? ServerConnectionEvent.DeviceConnected }
                    .map { it.connection }
                    .onEach {
                        Timber.d("Device connected: ${it.device}")
                        if (connections[it.device.address]?.isConnected == true) {
                            Timber.w("Connection already exists for device ${it.device.address}")
                            return@onEach
                        }
                        val connection = PPoGServiceConnection(it)
                        connections[it.device.address] = connection
                    }
                    .launchIn(serverScope)
        }
        scope = serverScope
        _state.value = State.OPEN
    }

    suspend fun sendMessageToDevice(deviceAddress: String, packet: ByteArray): Boolean {
        val connection = connections[deviceAddress] ?: run {
            Timber.w("Tried to send message but no connection for device $deviceAddress")
            return false
        }
        return connection.sendMessage(packet)
    }

    fun rxFlowFor(deviceAddress: String): Flow<ByteArray>? {
        return connections[deviceAddress]?.incomingPebblePacketData
    }

    override fun close() {
        try {
            server?.stopServer()
            scope?.cancel("GattServer closed")
        } catch (e: SecurityException) {
            Timber.w(e, "Failed to close GATT server")
        }
        connections.clear()
        server = null
        scope = null
        _state.value = State.CLOSED
    }
}