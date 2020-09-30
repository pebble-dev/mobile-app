package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothDevice

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    class Connecting(val watch: BluetoothDevice) : ConnectionState()
    class Connected(val watch: BluetoothDevice) : ConnectionState()
}