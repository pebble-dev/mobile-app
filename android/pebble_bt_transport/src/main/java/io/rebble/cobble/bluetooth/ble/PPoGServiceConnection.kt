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

class PPoGServiceConnection(parentScope: CoroutineScope, private val ppogService: PPoGService, val device: BluetoothDevice, private val deviceEventFlow: Flow<ServiceEvent>): Closeable {
    private val connectionScope = CoroutineScope(parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job]))
    private val ppogSession = PPoGSession(connectionScope, this, 23)
    private suspend fun runConnection() {
        deviceEventFlow.collect {
            when (it) {
                is CharacteristicReadEvent -> {
                    if (it.characteristic.uuid.toString() == LEConstants.UUIDs.META_CHARACTERISTIC_SERVER) {
                        it.respond(makeMetaResponse())
                    } else {
                        Timber.w("Unknown characteristic read request: ${it.characteristic.uuid}")
                        it.respond(CharacteristicResponse.Failure)
                    }
                }
                is CharacteristicWriteEvent -> {
                    if (it.characteristic.uuid.toString() == LEConstants.UUIDs.PPOGATT_DEVICE_CHARACTERISTIC_SERVER) {
                        try {
                            ppogSession.handleData(it.value)
                            it.respond(BluetoothGatt.GATT_SUCCESS)
                        } catch (e: Exception) {
                            it.respond(BluetoothGatt.GATT_FAILURE)
                            throw e
                        }
                    } else {
                        Timber.w("Unknown characteristic write request: ${it.characteristic.uuid}")
                        it.respond(BluetoothGatt.GATT_FAILURE)
                    }
                }
                is MtuChangedEvent -> {
                    ppogSession.mtu = it.mtu
                }
            }
        }
    }

    private fun makeMetaResponse(): CharacteristicResponse {
        return CharacteristicResponse(BluetoothGatt.GATT_SUCCESS, 0, LEConstants.SERVER_META_RESPONSE)
    }

    suspend fun start(): Flow<PebblePacket> {
        connectionScope.launch {
            runConnection()
        }
        return ppogSession.openPacketFlow()
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    suspend fun writeDataRaw(data: ByteArray): Boolean {
        val result = CompletableDeferred<Boolean>()
        val job = connectionScope.launch {
            val evt = deviceEventFlow.filterIsInstance<NotificationSentEvent>().first()
            result.complete(evt.status == BluetoothGatt.GATT_SUCCESS)
        }
        if (!ppogService.sendData(device, data)) {
            job.cancel()
            return false
        }
        if (!result.await()) {
            return false
        }
        return true
    }

    suspend fun sendPebblePacket(packet: PebblePacket) {
        val data = packet.serialize().asByteArray()
        ppogSession.sendData(data)
    }
    override fun close() {
        connectionScope.cancel()
    }
}