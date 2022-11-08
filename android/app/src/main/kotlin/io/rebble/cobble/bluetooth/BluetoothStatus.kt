package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.IntentFilter
import io.rebble.cobble.util.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
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