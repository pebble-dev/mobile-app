package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothAdapter
import kotlinx.coroutines.delay

@Suppress("DEPRECATION") // we are an exception as a test
suspend fun restartBluetooth(bluetoothAdapter: BluetoothAdapter) {
    bluetoothAdapter.disable()
    while (bluetoothAdapter.isEnabled) {
        delay(100)
    }
    delay(1000)
    bluetoothAdapter.enable()
    while (!bluetoothAdapter.isEnabled) {
        delay(100)
    }
}