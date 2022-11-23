package io.rebble.cobble.bluetooth.gatt

import io.rebble.libpebblecommon.ProtocolHandlerImpl
import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.createTestPebbleProtocol(protocolHandler: PPoGATTProtocolHandler): ProtocolHandlerImpl {
    val pebbleProtocol = ProtocolHandlerImpl()

    protocolHandler.rxPebblePacketFlow.onEach {
        Timber.d("RX ${PebblePacket.deserialize(it.asUByteArray())::class.simpleName}")
        pebbleProtocol.receivePacket(it.asUByteArray())
    }.launchIn(backgroundScope)


    backgroundScope.launch {
        pebbleProtocol.startPacketSendingLoop {
            protocolHandler.sendPebblePacket(it.asByteArray())
        }
    }

    runCurrent()

    return pebbleProtocol
}