package io.rebble.cobble.bluetooth.ble

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import io.rebble.cobble.bluetooth.BlueIO
import io.rebble.cobble.bluetooth.PebbleDevice
import io.rebble.cobble.bluetooth.SingleConnectionStatus
import io.rebble.cobble.bluetooth.workarounds.UnboundWatchBeforeConnecting
import io.rebble.cobble.bluetooth.workarounds.WorkaroundDescriptor
import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.IOException
import kotlin.coroutines.CoroutineContext

/**
 * Bluetooth Low Energy driver for Pebble watches
 * @param context Android context
 * @param protocolHandler Protocol handler for Pebble communication
 * @param workaroundResolver Function to check if a workaround is enabled
 */
class BlueLEDriver(
        coroutineContext: CoroutineContext = Dispatchers.IO,
        private val context: Context,
        private val protocolHandler: ProtocolHandler,
        private val workaroundResolver: (WorkaroundDescriptor) -> Boolean
): BlueIO {
    private val scope = CoroutineScope(coroutineContext)
    @OptIn(FlowPreview::class)
    @Throws(SecurityException::class)
    override fun startSingleWatchConnection(device: PebbleDevice): Flow<SingleConnectionStatus> {
        require(!device.emulated)
        require(device.bluetoothDevice != null)
        return flow {
            GattServerManager.initIfNeeded(context, scope)
            val gatt = device.bluetoothDevice.connectGatt(context, workaroundResolver(UnboundWatchBeforeConnecting))
                    ?: throw IOException("Failed to connect to device")
            emit(SingleConnectionStatus.Connecting(device))
            val connector = PebbleLEConnector(gatt, context, scope)
            var success = false
            connector.connect().collect {
                when (it) {
                    PebbleLEConnector.ConnectorState.CONNECTING -> Timber.d("PebbleLEConnector is connecting")
                    PebbleLEConnector.ConnectorState.PAIRING -> Timber.d("PebbleLEConnector is pairing")
                    PebbleLEConnector.ConnectorState.CONNECTED -> {
                        Timber.d("PebbleLEConnector connected watch, waiting for watch")
                        PPoGLinkStateManager.updateState(device.address, PPoGLinkState.ReadyForSession)
                        success = true
                    }
                }
            }
            check(success) { "Failed to connect to watch" }
            GattServerManager.getGattServer()?.getServer()?.connect(device.bluetoothDevice, true)
            try {
                withTimeout(10000) {
                    val result = PPoGLinkStateManager.getState(device.address).first { it != PPoGLinkState.ReadyForSession }
                    if (result == PPoGLinkState.SessionOpen) {
                        Timber.d("Session established")
                        emit(SingleConnectionStatus.Connected(device))
                    } else {
                        throw IOException("Failed to establish session")
                    }
                }
            } catch (e: TimeoutCancellationException) {
                throw IOException("Failed to establish session, timeout")
            }

            val sendLoop = scope.launch {
                protocolHandler.startPacketSendingLoop {
                    Timber.v("Sending packet")
                    GattServerManager.ppogService!!.emitPacket(device.bluetoothDevice, it.asByteArray())
                    Timber.v("Sent packet")
                    return@startPacketSendingLoop true
                }
            }
            GattServerManager.ppogService?.rxFlowFor(device.bluetoothDevice)!!.collect {
                when (it) {
                    is PPoGService.PPoGConnectionEvent.PacketReceived -> {
                        protocolHandler.receivePacket(it.packet.asUByteArray())
                    }
                    is PPoGService.PPoGConnectionEvent.LinkError -> {
                        Timber.e(it.error, "Link error")
                        throw it.error
                    }
                }
            }
            sendLoop.cancel()
        }
    }
}