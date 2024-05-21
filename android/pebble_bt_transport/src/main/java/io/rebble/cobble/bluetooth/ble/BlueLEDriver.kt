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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.io.IOException

/**
 * Bluetooth Low Energy driver for Pebble watches
 * @param context Android context
 * @param protocolHandler Protocol handler for Pebble communication
 * @param workaroundResolver Function to check if a workaround is enabled
 */
class BlueLEDriver(
        private val context: Context,
        private val protocolHandler: ProtocolHandler,
        private val scope: CoroutineScope,
        private val workaroundResolver: (WorkaroundDescriptor) -> Boolean
): BlueIO {
    @OptIn(FlowPreview::class)
    @Throws(SecurityException::class)
    override fun startSingleWatchConnection(device: PebbleDevice): Flow<SingleConnectionStatus> {
        require(!device.emulated)
        require(device.bluetoothDevice != null)
        return flow {
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
            withTimeout(10000) {
                PPoGLinkStateManager.getState(device.address).first { it == PPoGLinkState.SessionOpen }
            }
            emit(SingleConnectionStatus.Connected(device))
        }
    }
}