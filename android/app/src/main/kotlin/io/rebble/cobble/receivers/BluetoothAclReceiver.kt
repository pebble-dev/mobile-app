package io.rebble.cobble.receivers

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.bluetooth.ConnectionState
import kotlinx.coroutines.*

@OptIn(ExperimentalCoroutinesApi::class)
class BluetoothAclReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != BluetoothDevice.ACTION_ACL_CONNECTED) {
            return
        }/*

        val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                ?: return

        val component = (context.applicationContext as CobbleApplication).component

        val pairedStorage = component.createPairedStorage()
        val connectionLooper = component.createConnectionLooper()

        if (pairedStorage.getMacAddressOfDefaultPebble() == device.address &&
                connectionLooper.connectionState.value is ConnectionState.Connecting) {
            // After ACL is established, Pebble still needs some time to initialize
            // Attempt connection after one second

            GlobalScope.launch(Dispatchers.Main.immediate) {
                delay(1000)

                if (connectionLooper.connectionState.value is ConnectionState.Connecting) {
                    connectionLooper.connectToWatch(device.address)
                }
            }
        }*/
    }
}