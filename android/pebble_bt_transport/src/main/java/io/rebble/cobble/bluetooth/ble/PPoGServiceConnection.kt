package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import androidx.annotation.RequiresPermission
import io.rebble.cobble.bluetooth.ble.util.chunked
import io.rebble.libpebblecommon.ble.LEConstants
import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.Closeable
import java.util.UUID

class PPoGServiceConnection(val connectionScope: CoroutineScope, private val ppogService: PPoGService, val device: BluetoothDevice, private val deviceEventFlow: Flow<ServiceEvent>): Closeable {
    private val ppogSession = PPoGSession(connectionScope, device, LEConstants.DEFAULT_MTU)
    var debouncedCloseJob: Job? = null

    companion object {
        val ppogCharacteristicUUID = UUID.fromString(LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER)
        val configurationDescriptorUUID = UUID.fromString(LEConstants.UUIDs.CHARACTERISTIC_CONFIGURATION_DESCRIPTOR)
    }
    private suspend fun runConnection() = deviceEventFlow.onEach {
        when (it) {
            is CharacteristicWriteEvent -> {
                if (it.characteristic.uuid == ppogCharacteristicUUID) {
                    ppogSession.handlePacket(it.value)
                } else {
                    Timber.w("Unknown characteristic write request: ${it.characteristic.uuid}")
                    it.respond(BluetoothGatt.GATT_FAILURE)
                }
            }
            is DescriptorWriteEvent -> {
                if (it.descriptor.uuid == configurationDescriptorUUID && it.descriptor.characteristic.uuid == ppogCharacteristicUUID) {
                    it.respond(BluetoothGatt.GATT_SUCCESS)
                } else {
                    Timber.w("Unknown descriptor write request: ${it.descriptor.uuid}")
                    it.respond(BluetoothGatt.GATT_FAILURE)
                }
            }
            is MtuChangedEvent -> {
                ppogSession.setMTU(it.mtu)
            }
        }
    }.catch {
        Timber.e(it)
        connectionScope.cancel("Error in device event flow", it)
    }.launchIn(connectionScope)

    /**
     * Start the connection and return a flow of received data (pebble packets)
     * @return Flow of received serialized pebble packets
     */
    suspend fun start(): Flow<ByteArray> {
        runConnection()
        return ppogSession.flow().onEach {
            if (it is PPoGSession.PPoGSessionResponse.WritePPoGCharacteristic) {
                it.result.complete(ppogService.sendData(device, it.data))
            }
        }.filterIsInstance<PPoGSession.PPoGSessionResponse.PebblePacket>().map { it.packet }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    suspend fun writeDataRaw(data: ByteArray): Boolean {
        return ppogService.sendData(device, data)
    }

    suspend fun sendPebblePacket(packet: ByteArray) {
        ppogSession.sendMessage(packet)
    }
    override fun close() {
        connectionScope.cancel()
    }

    suspend fun debouncedClose(): Boolean {
        debouncedCloseJob?.cancel()
        val job = connectionScope.launch {
            delay(1000)
            close()
        }
        debouncedCloseJob = job
        try {
            debouncedCloseJob?.join()
        } catch (e: CancellationException) {
            return false
        }
        return true
    }

    fun resetDebouncedClose() {
        debouncedCloseJob?.cancel()
    }
}