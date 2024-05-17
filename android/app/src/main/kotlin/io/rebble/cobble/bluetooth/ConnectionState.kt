package io.rebble.cobble.bluetooth

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    class WaitingForBluetoothToEnable(val watch: PebbleDevice?) : ConnectionState()
    class WaitingForReconnect(val watch: PebbleDevice?) : ConnectionState()
    class Connecting(val watch: PebbleDevice?) : ConnectionState()
    class Negotiating(val watch: PebbleDevice?) : ConnectionState()
    class Connected(val watch: PebbleDevice) : ConnectionState()
    class RecoveryMode(val watch: PebbleDevice) : ConnectionState()
}

val ConnectionState.watchOrNull: PebbleDevice?
    get() {
        return when (this) {
            is ConnectionState.Connecting -> watch
            is ConnectionState.Negotiating -> watch
            is ConnectionState.WaitingForReconnect -> watch
            is ConnectionState.Connected -> watch
            is ConnectionState.WaitingForBluetoothToEnable -> watch
            is ConnectionState.RecoveryMode -> watch
            ConnectionState.Disconnected -> null
        }
    }