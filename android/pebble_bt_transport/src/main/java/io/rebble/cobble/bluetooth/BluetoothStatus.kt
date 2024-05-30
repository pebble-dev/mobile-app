package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.IntentFilter
import io.rebble.cobble.bluetooth.util.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

fun getBluetoothStatus(context: Context): Flow<Boolean> {
    return IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED).asFlow(context)
            .map {
                it.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF) ==
                        BluetoothAdapter.STATE_ON
            }
            .onStart {
                emit(BluetoothAdapter.getDefaultAdapter()?.isEnabled == true)
            }
}

class BluetoothDevicePairEvent(val device: BluetoothDevice, val bondState: Int, val unbondReason: Int?)

fun getBluetoothDevicePairEvents(context: Context, address: String): Flow<BluetoothDevicePairEvent> {
    return IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED).asFlow(context)
            .map {
                BluetoothDevicePairEvent(
                        it.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!,
                        it.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE),
                        it.getIntExtra("android.bluetooth.device.extra.REASON", -1).takeIf { it != -1 }
                )
            }
            .filter {
                it.device.address == address
            }
}