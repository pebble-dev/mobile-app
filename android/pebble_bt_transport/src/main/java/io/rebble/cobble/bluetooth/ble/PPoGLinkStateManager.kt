package io.rebble.cobble.bluetooth.ble

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

object PPoGLinkStateManager {
    private val states = mutableMapOf<String, Channel<PPoGLinkState>>()

    fun getState(deviceAddress: String): Flow<PPoGLinkState> {
        return states.getOrPut(deviceAddress) {
            Channel(Channel.BUFFERED)
        }.consumeAsFlow()
    }

    fun removeState(deviceAddress: String) {
        states.remove(deviceAddress)
    }

    fun updateState(deviceAddress: String, state: PPoGLinkState) {
        states.getOrPut(deviceAddress) {
            Channel(Channel.BUFFERED)
        }.trySend(state)
    }
}

enum class PPoGLinkState {
    Closed,
    ReadyForSession,
    SessionOpen
}