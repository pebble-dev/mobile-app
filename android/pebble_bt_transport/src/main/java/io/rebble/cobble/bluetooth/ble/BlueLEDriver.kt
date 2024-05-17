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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Bluetooth Low Energy driver for Pebble watches
 * @param context Android context
 * @param protocolHandler Protocol handler for Pebble communication
 * @param workaroundResolver Function to check if a workaround is enabled
 */
class BlueLEDriver(
        private val context: Context,
        private val protocolHandler: ProtocolHandler,
        private val workaroundResolver: (WorkaroundDescriptor) -> Boolean
): BlueIO {
    @OptIn(FlowPreview::class)
    @Throws(SecurityException::class)
    override fun startSingleWatchConnection(device: PebbleDevice): Flow<SingleConnectionStatus> {
        require(!device.emulated)
        require(device.bluetoothDevice != null)
        return flow {
            val gatt = device.bluetoothDevice.connectGatt(context, workaroundResolver(UnboundWatchBeforeConnecting))
            emit(SingleConnectionStatus.Connecting(device))
        }
    }
}