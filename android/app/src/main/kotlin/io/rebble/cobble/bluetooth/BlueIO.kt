package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

interface BlueIO {
    @FlowPreview
    fun startSingleWatchConnection(device: BluetoothDevice): Flow<SingleConnectionStatus>
}

sealed class SingleConnectionStatus {
    class Connecting(val watch: BluetoothDevice) : SingleConnectionStatus()
    class Connected(val watch: BluetoothDevice) : SingleConnectionStatus()
}