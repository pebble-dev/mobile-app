package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.Flow

interface BlueIO {
    fun startSingleWatchConnection(device: BluetoothDevice): Flow<SingleConnectionStatus>
    suspend fun sendPacket(bytes: ByteArray): Boolean
}

sealed class SingleConnectionStatus {
    class Connecting(val watch: BluetoothDevice) : SingleConnectionStatus()
    class Connected(val watch: BluetoothDevice) : SingleConnectionStatus()
}