package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothDevice

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    class WaitingForBluetoothToEnable(val watch: BluetoothDevice?) : ConnectionState()
    class WaitingForReconnect(val watch: BluetoothDevice?) : ConnectionState()
    class Connecting(val watch: BluetoothDevice?) : ConnectionState()
    class Connected(val watch: BluetoothDevice) : ConnectionState()
}

val ConnectionState.watchOrNull: BluetoothDevice?
    get() {
        return when (this) {
            is ConnectionState.Connecting -> watch
            is ConnectionState.WaitingForReconnect -> watch
            is ConnectionState.Connected -> watch
            is ConnectionState.WaitingForBluetoothToEnable -> watch
            ConnectionState.Disconnected -> null
        }
    }