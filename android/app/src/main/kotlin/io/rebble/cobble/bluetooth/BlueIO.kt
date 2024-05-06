package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

interface BlueIO {
    @FlowPreview
    fun startSingleWatchConnection(device: PebbleBluetoothDevice): Flow<SingleConnectionStatus>
}

data class PebbleBluetoothDevice (
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
    class Connecting(val watch: PebbleBluetoothDevice) : SingleConnectionStatus()
    class Connected(val watch: PebbleBluetoothDevice) : SingleConnectionStatus()
}