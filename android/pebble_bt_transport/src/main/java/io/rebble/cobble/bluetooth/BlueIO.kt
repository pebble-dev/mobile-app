package io.rebble.cobble.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
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

    override fun toString(): String {
        val start = "< PebbleDevice emulated=$emulated, address=$address, bluetoothDevice=< BluetoothDevice address=${bluetoothDevice?.address}"
        return try {
            "$start, name=${bluetoothDevice?.name}, type=${bluetoothDevice?.type} > >"
        } catch (e: SecurityException) {
            "$start, name=unknown, type=unknown > >"
        }
    }
}

sealed class SingleConnectionStatus {
    class Connecting(val watch: PebbleDevice) : SingleConnectionStatus()
    class Connected(val watch: PebbleDevice) : SingleConnectionStatus()
}