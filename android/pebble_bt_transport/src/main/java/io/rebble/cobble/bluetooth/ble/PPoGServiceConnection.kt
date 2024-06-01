package io.rebble.cobble.bluetooth.ble

import io.rebble.libpebblecommon.ble.LEConstants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.kotlin.ble.core.errors.GattOperationException
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBluetoothGattConnection
import timber.log.Timber
import java.io.Closeable
import java.util.UUID

@OptIn(FlowPreview::class)
class PPoGServiceConnection(private val serverConnection: ServerBluetoothGattConnection, ioDispatcher: CoroutineDispatcher = Dispatchers.IO): Closeable {
    private val scope = CoroutineScope(ioDispatcher)
    private val ppogSession = PPoGSession(scope, serverConnection.device.address, LEConstants.DEFAULT_MTU)

    companion object {
        val ppogServiceUUID: UUID = UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_SERVICE_UUID_SERVER)
        val ppogCharacteristicUUID: UUID = UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER)
        val configurationDescriptorUUID: UUID = UUID.fromString(LEConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR)
        val metaCharacteristicUUID: UUID = UUID.fromString(LEConstants.UUIDs.META_CHARACTERISTIC_SERVER)
    }

    private val _latestPebblePacket = MutableStateFlow<ByteArray?>(null)
    val latestPebblePacket: Flow<ByteArray?> = _latestPebblePacket

    val isConnected: Boolean
        get() = scope.isActive

    private val notificationsEnabled = MutableStateFlow(false)

    init {
        Timber.d("PPoGServiceConnection created with ${serverConnection.device}: PHY (RX ${serverConnection.rxPhy} TX ${serverConnection.txPhy})")
        //TODO: Uncomment me
        //serverConnection.connectionProvider.updateMtu(LEConstants.TARGET_MTU)
        serverConnection.services.findService(ppogServiceUUID)?.let { service ->
            check(service.findCharacteristic(metaCharacteristicUUID) != null) { "Meta characteristic missing" }
            service.findCharacteristic(ppogCharacteristicUUID)?.let { characteristic ->
                serverConnection.connectionProvider.mtu.onEach {
                    ppogSession.mtu = it
                }.launchIn(scope)
                characteristic.value.onEach {
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
                            _latestPebblePacket.value = it.packet
                        }
                    }
                }.launchIn(scope)
                serverConnection.connectionProvider.connectionStateWithStatus
                        .filterNotNull()
                        .debounce(1000) // Debounce to ignore quick reconnects
                        .onEach {
                            Timber.v("(${serverConnection.device}) New connection state: ${it.state} ${it.status}")
                        }
                        .filter { it.state == GattConnectionState.STATE_DISCONNECTED }
                        .onEach {
                            Timber.i("(${serverConnection.device}) Connection lost")
                            scope.cancel("Connection lost")
                        }
                        .launchIn(scope)
            } ?: throw IllegalStateException("PPOG Characteristic missing")
        } ?: throw IllegalStateException("PPOG Service missing")
    }

    override fun close() {
        scope.cancel("Closed")
    }

    suspend fun sendMessage(packet: ByteArray): Boolean {
        return ppogSession.sendMessage(packet)
    }
}