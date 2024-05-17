package io.rebble.cobble.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

interface BlueIO {
    @FlowPreview
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun startSingleWatchConnection(device: PebbleDevice): Flow<SingleConnectionStatus>
}

data class PebbleDevice (
        val bluetoothDevice: BluetoothDevice?,
        val emulated: Boolean,
        val address: String
) {
    constructor(bluetoothDevice: BluetoothDevice?, emulated: Boolean = false) :
            this(
                    bluetoothDevice,
                    emulated,
                    bluetoothDevice?.address ?: throw IllegalArgumentException()
            )
}

sealed class SingleConnectionStatus {
    class Connecting(val watch: PebbleDevice) : SingleConnectionStatus()
    class Connected(val watch: PebbleDevice) : SingleConnectionStatus()
}