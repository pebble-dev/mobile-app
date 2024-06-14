package io.rebble.cobble.bluetooth.ble

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PPoGLinkStateManager {
    private val states = mutableMapOf<String, MutableStateFlow<PPoGLinkState>>()

    fun getState(deviceAddress: String): StateFlow<PPoGLinkState> {
        return states.getOrPut(deviceAddress) {
            MutableStateFlow(PPoGLinkState.Closed)
        }.asStateFlow()
    }

    fun updateState(deviceAddress: String, state: PPoGLinkState) {
        states.getOrPut(deviceAddress) {
            MutableStateFlow(PPoGLinkState.Closed)
        }.value = state
    }
}

enum class PPoGLinkState {
    Closed,
    ReadyForSession,
    SessionOpen
}