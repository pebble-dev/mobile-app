package io.rebble.cobble.bluetooth.ble

import io.rebble.libpebblecommon.ble.LEConstants
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.kotlin.ble.core.errors.GattOperationException
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBluetoothGattConnection
import timber.log.Timber
import java.io.Closeable
import java.util.UUID

@OptIn(FlowPreview::class)
class PPoGServiceConnection(
    private var serverConnection: ServerBluetoothGattConnection,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : Closeable {
    private var scope = serverConnection.connectionScope + ioDispatcher + CoroutineName("PPoGServiceConnection-${serverConnection.device.address}")
    private val sessionScope = CoroutineScope(ioDispatcher) + CoroutineName("PPoGSession-${serverConnection.device.address}")
    private val ppogSession =
        PPoGSession(sessionScope, serverConnection.device.address, LEConstants.DEFAULT_MTU)

    val device get() = serverConnection.device

    companion object {
        val ppogServiceUUID: UUID =
            UUID.fromString(
                LEConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER
            )
        val ppogCharacteristicUUID: UUID =
            UUID.fromString(
                LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER
            )
        val configurationDescriptorUUID: UUID =
            UUID.fromString(
                LEConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR
            )
        val metaCharacteristicUUID: UUID =
            UUID.fromString(
                LEConstants.UUIDs.META_CHARACTERISTIC_SERVER
            )
    }

    private val _incomingPebblePackets = Channel<ByteArray>(Channel.BUFFERED)
    val incomingPebblePacketData: Flow<ByteArray> = _incomingPebblePackets.receiveAsFlow()

    // Make our own connection state flow that debounces the connection state, as we might recreate the connection but only want to cancel everything if it doesn't reconnect
    private val connectionStateDebounced = MutableStateFlow<GattConnectionStateWithStatus?>(null)

    val isConnected: Boolean
        get() = scope.isActive
    val isStillValid: Boolean
        get() = sessionScope.isActive

    private val notificationsEnabled = MutableStateFlow(false)
    private var lastNotify: DataByteArray? = null

    init {
        connectionStateDebounced
            .filterNotNull()
            .debounce(1000)
            .onEach {
                Timber.v(
                    "(${serverConnection.device}) New connection state: ${it.state} ${it.status}"
                )
            }
            .filter { it.state == GattConnectionState.STATE_DISCONNECTED }
            .onEach {
                Timber.i("(${serverConnection.device}) Connection lost")
                scope.cancel("Connection lost")
                sessionScope.cancel("Connection lost")
            }
            .launchIn(sessionScope)
        launchFlows()
    }

    private fun launchFlows() {
        Timber.d("PPoGServiceConnection created with ${serverConnection.device}")
        serverConnection.connectionProvider.updateMtu(LEConstants.TARGET_MTU)
        serverConnection.services.findService(ppogServiceUUID)?.let { service ->
            check(service.findCharacteristic(metaCharacteristicUUID) != null) { "Meta characteristic missing" }
            service.findCharacteristic(ppogCharacteristicUUID)?.let { characteristic ->
                serverConnection.connectionProvider.mtu.onEach {
                    ppogSession.mtu = it
                }.launchIn(scope)
                characteristic.value
                    .filter { it != lastNotify } // Ignore echo
                    .onEach {
                        ppogSession.handlePacket(it.value.clone())
                    }.launchIn(scope)
                characteristic.findDescriptor(configurationDescriptorUUID)?.value?.onEach {
                    val value = it.getIntValue(IntFormat.FORMAT_UINT8, 0)
                    Timber.i("(${serverConnection.device}) PPOG Notify changed: $value")
                    notificationsEnabled.value = value == 1
                }?.launchIn(scope)
                ppogSession.flow().onEach {
                    when (it) {
                        is PPoGSession.PPoGSessionResponse.WritePPoGCharacteristic -> {
                            try {
                                if (notificationsEnabled.value) {
                                    lastNotify = DataByteArray(it.data)
                                    characteristic.setValueAndNotifyClient(DataByteArray(it.data))
                                    it.result.complete(true)
                                } else {
                                    Timber.w("(${serverConnection.device}) Tried to send PPoG packet while notifications are disabled")
                                    it.result.complete(false)
                                }
                            } catch (e: GattOperationException) {
                                Timber.e(e, "(${serverConnection.device}) Failed to send PPoG characteristic notification")
                                it.result.complete(false)
                            }
                        }

                        is PPoGSession.PPoGSessionResponse.PebblePacket -> {
                            _incomingPebblePackets.trySend(it.packet).getOrThrow()
                        }
                    }
                }.launchIn(scope)
                serverConnection.connectionProvider.connectionStateWithStatus
                    .onEach {
                        connectionStateDebounced.value = it
                    }
                    .launchIn(scope)
            } ?: throw IllegalStateException("PPOG Characteristic missing")
        } ?: throw IllegalStateException("PPOG Service missing")
    }

    fun reinit(serverConnection: ServerBluetoothGattConnection) {
        this.serverConnection = serverConnection
        scope.cancel("Reinit")
        scope = serverConnection.connectionScope + ioDispatcher + CoroutineName("PPoGServiceConnection-${serverConnection.device.address}")
    }

    override fun close() {
        scope.cancel("Closed")
        sessionScope.cancel("Closed")
    }

    suspend fun sendMessage(packet: ByteArray): Boolean {
        ppogSession.stateManager.stateFlow.first {
            it == PPoGSession.State.Open
        } // Wait for session to open, otherwise packet will be dropped
        return ppogSession.sendMessage(packet)
    }

    suspend fun requestReset() {
        ppogSession.requestReset()
    }
}