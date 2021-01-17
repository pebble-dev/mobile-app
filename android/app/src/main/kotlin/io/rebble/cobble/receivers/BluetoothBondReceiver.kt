package io.rebble.cobble.receivers

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

class BluetoothBondReceiver(private val targetAddr: String, private val context: Context) : BroadcastReceiver() {
    companion object {
        fun registerBondReceiver(context: Context, targetAddress: String): BluetoothBondReceiver {
            val receiver = BluetoothBondReceiver(targetAddress, context)
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            context.registerReceiver(receiver, intentFilter)
            return receiver
        }
    }

    private var bondEvent = MutableStateFlow<Int?>(null)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
            return
        }
        val device = intent.extras?.get(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?
        if (device != null && device.address == targetAddr) {
            val state = intent.extras?.get(BluetoothDevice.EXTRA_BOND_STATE) as Int?
            if (state != null) bondEvent.value = state
        }
    }

    suspend fun awaitBondResult(): Int {
        return bondEvent.filterNotNull().first { it != BluetoothDevice.BOND_BONDING }
    }

    fun reset() {
        bondEvent.value = null
    }

    fun unregister() {
        context.unregisterReceiver(this)
    }
}