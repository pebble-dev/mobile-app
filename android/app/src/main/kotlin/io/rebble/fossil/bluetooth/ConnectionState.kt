package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothDevice

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    class Connecting(val watch: BluetoothDevice?) : ConnectionState()
    class Connected(val watch: BluetoothDevice) : ConnectionState()
}

val ConnectionState.watchOrNull: BluetoothDevice?
    get() {
        return when (this) {
            is ConnectionState.Connecting -> watch
            is ConnectionState.Connected -> watch
            ConnectionState.Disconnected -> null
        }
    }