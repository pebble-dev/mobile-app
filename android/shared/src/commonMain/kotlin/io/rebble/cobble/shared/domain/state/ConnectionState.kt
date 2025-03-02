package io.rebble.cobble.shared.domain.state

import io.rebble.cobble.shared.domain.common.PebbleDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

open class ConnectionState {
    object Disconnected : ConnectionState()

    data class WaitingForTransport(val address: String) : ConnectionState()

    data class WaitingForReconnect(val watch: PebbleDevice?) : ConnectionState()

    data class Connecting(val watch: PebbleDevice?) : ConnectionState()

    data class Negotiating(val watch: PebbleDevice?) : ConnectionState()

    data class Connected(val watch: PebbleDevice) : ConnectionState()

    data class RecoveryMode(val watch: PebbleDevice) : ConnectionState()
}

val ConnectionState.watchOrNull: PebbleDevice?
    get() =
        when (this) {
            is ConnectionState.WaitingForReconnect -> watch
            is ConnectionState.Connecting -> watch
            is ConnectionState.Negotiating -> watch
            is ConnectionState.Connected -> watch
            is ConnectionState.RecoveryMode -> watch
            else -> null
        }

object ConnectionStateManager : KoinComponent {
    val connectionState: MutableStateFlow<ConnectionState> by inject(named("connectionState"))

    val isConnected: StateFlow<Boolean> by inject(named("isConnected"))
}