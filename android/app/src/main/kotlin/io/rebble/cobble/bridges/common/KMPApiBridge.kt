package io.rebble.cobble.bridges.common

import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.shared.domain.state.CurrentToken
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class KMPApiBridge @Inject constructor(
        private val tokenState: MutableStateFlow<CurrentToken>,
        bridgeLifecycleController: BridgeLifecycleController
): FlutterBridge, Pigeons.KMPApi {

    init {
        bridgeLifecycleController.setupControl(Pigeons.KMPApi::setup, this)
    }

    override fun updateToken(token: Pigeons.StringWrapper) {
        tokenState.value = token.value?.let { CurrentToken.LoggedIn(it) } ?: CurrentToken.LoggedOut
    }
}