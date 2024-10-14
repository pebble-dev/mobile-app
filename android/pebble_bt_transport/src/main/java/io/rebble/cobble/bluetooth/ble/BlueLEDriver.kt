package io.rebble.cobble.bluetooth.ble

import android.content.Context
import io.rebble.cobble.bluetooth.*
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.workarounds.UnboundWatchBeforeConnecting
import io.rebble.cobble.shared.workarounds.WorkaroundDescriptor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.coroutines.CoroutineContext

/**
 * Bluetooth Low Energy driver for Pebble watches
 * @param context Android context
 * @param protocolHandler Protocol handler for Pebble communication
 * @param workaroundResolver Function to check if a workaround is enabled
 */
@OptIn(ExperimentalUnsignedTypes::class)
class BlueLEDriver(
        coroutineContext: CoroutineContext = Dispatchers.IO,
        private val context: Context,
        private val pebbleDevice: PebbleDevice,
        private val gattServerManager: GattServerManager,
        private val incomingPacketsListener: MutableSharedFlow<ByteArray>,
        private val workaroundResolver: (WorkaroundDescriptor) -> Boolean
) : BlueIO {
    private val scope = CoroutineScope(coroutineContext)

    @OptIn(FlowPreview::class)
    @Throws(SecurityException::class)
    override fun startSingleWatchConnection(device: PebbleDevice): Flow<SingleConnectionStatus> {
        require(device is BluetoothPebbleDevice) { "Device must be BluetoothPebbleDevice" }
        return flow {
            val gattServer = gattServerManager.gattServer.first()
            if (gattServer.state.value == NordicGattServer.State.INIT) {
                Timber.i("Waiting for GATT server to open")
                withTimeout(1000) {
                    gattServer.state.first { it == NordicGattServer.State.OPEN }
                }
            }
            check(gattServer.state.value == NordicGattServer.State.OPEN) { "GATT server is not open" }

            var gatt: BlueGATTConnection = device.bluetoothDevice.connectGatt(context, workaroundResolver(
                UnboundWatchBeforeConnecting
            ))
                    ?: throw IOException("Failed to connect to device")
            try {
                emit(SingleConnectionStatus.Connecting(device))

                val connector = PebbleLEConnector(gatt, context, scope)
                var success = false
                connector.connect()
                        .catch {
                            Timber.e(it, "LEConnector failed to connect")
                            throw it
                        }
                        .collect {
                            when (it) {
                                PebbleLEConnector.ConnectorState.CONNECTING -> Timber.d("PebbleLEConnector ${connector} is connecting")
                                PebbleLEConnector.ConnectorState.PAIRING -> Timber.d("PebbleLEConnector is pairing")
                                PebbleLEConnector.ConnectorState.CONNECTED -> {
                                    Timber.d("PebbleLEConnector connected watch, waiting for watch")
                                    PPoGLinkStateManager.updateState(device.address, PPoGLinkState.ReadyForSession)
                                    success = true
                                }
                            }
                        }

                check(success) { "Failed to connect to watch" }
                val protocolInputStream = PipedInputStream()
                val protocolOutputStream = PipedOutputStream()
                val rxStream = PipedOutputStream(protocolInputStream)

                val protocolIO = ProtocolIO(
                        protocolInputStream.buffered(8192),
                        protocolOutputStream.buffered(8192),
                        pebbleDevice.protocolHandler,
                        incomingPacketsListener
                )
                try {
                    withTimeout(20000) {
                        val result = PPoGLinkStateManager.getState(device.address).first { it != PPoGLinkState.ReadyForSession }
                        if (result == PPoGLinkState.SessionOpen) {
                            Timber.d("Session established")
                        } else {
                            throw IOException("Failed to establish session")
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    throw IOException("Failed to establish session, timed out")
                }
                val rxJob = gattServer.rxFlowFor(device.address)?.onEach {
                    rxStream.write(it)
                }?.flowOn(Dispatchers.IO)?.launchIn(scope)
                        ?: throw IOException("Failed to get rxFlow")
                val sendLoop = scope.launch(Dispatchers.IO) {
                    pebbleDevice.protocolHandler.startPacketSendingLoop {
                        gattServer.sendMessageToDevice(device.address, it.asByteArray())
                        return@startPacketSendingLoop true
                    }
                }
                emit(SingleConnectionStatus.Connected(device))
                protocolIO.readLoop()
                rxJob.cancel()
                sendLoop.cancel()
            } finally {
                gatt.close()
                Timber.d("Disconnected from watch")
            }
        }
                .flowOn(Dispatchers.IO)
    }
}