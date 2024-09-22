package io.rebble.cobble.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import androidx.annotation.RequiresPermission
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive

interface BlueIO {
    @FlowPreview
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun startSingleWatchConnection(device: PebbleDevice): Flow<SingleConnectionStatus>
}

class BluetoothPebbleDevice(
        val bluetoothDevice: BluetoothDevice,
        protocolHandler: ProtocolHandler,
        address: String
) : PebbleDevice(null, protocolHandler, address){

    override fun toString(): String {
        val start = "< BluetoothPebbleDevice, address=$address, connectionScopeActive=${connectionScope.value?.isActive}, bluetoothDevice=< BluetoothDevice address=${bluetoothDevice.address}"
        return try {
            "$start, name=${bluetoothDevice.name}, type=${bluetoothDevice.type} > >"
        } catch (e: SecurityException) {
            "$start, name=unknown, type=unknown > >"
        }
    }
}

sealed class SingleConnectionStatus {
    class Connecting(val watch: PebbleDevice) : SingleConnectionStatus()
    class Connected(val watch: PebbleDevice) : SingleConnectionStatus()
}