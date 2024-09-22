package io.rebble.cobble.shared.handlers

import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.libpebblecommon.packets.AppCustomizationSetStockAppTitleMessage
import io.rebble.libpebblecommon.packets.AppMessage
import io.rebble.libpebblecommon.packets.AppRunStateMessage
import io.rebble.libpebblecommon.packets.AppType
import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import io.rebble.libpebblecommon.services.app.AppRunStateService
import io.rebble.libpebblecommon.services.appmessage.AppMessageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.seconds

private data class AppMessageTimestamp(val app: Uuid, val timestamp: Long)



open class OutgoingMessage {
    data class Data(
            val uuid: Uuid,
            val transactionId: Int,
            val packet: AppMessage
    ) : OutgoingMessage()

    data class Ack(
            val packet: AppMessage
    ) : OutgoingMessage()

    data class Nack(
            val packet: AppMessage
    ) : OutgoingMessage()

    data class AppStart(
            val uuid: Uuid
    ) : OutgoingMessage()

    data class AppStop(
            val uuid: Uuid
    ) : OutgoingMessage()

    data class AppCustomize(
            val appType: AppType,
            val name: String
    ) : OutgoingMessage()
}

interface PlatformAppMessageIPC {
    fun sendPush(message: AppMessage.AppMessagePush)
    fun sendAck(message: AppMessage.AppMessageACK)
    fun sendNack(transactionId: Int)
    fun outgoingMessages(): Flow<OutgoingMessage>
    fun broadcastPebbleConnected()
    fun broadcastPebbleDisconnected()
}

class AppMessageHandler(
    private val pebbleDevice: PebbleDevice,
) : KoinComponent, CobbleHandler {
    private val platformAppMessageIPC: PlatformAppMessageIPC by inject()
    private var lastReceivedMessage: AppMessageTimestamp? = null
    private val outgoingMessages = platformAppMessageIPC.outgoingMessages().buffer()

    init {
        pebbleDevice.negotiationScope.launch {
            val deviceScope = pebbleDevice.connectionScope.filterNotNull().first()
            listenForIncomingPackets(deviceScope)

            listenForOutgoingMessages(deviceScope)

            sendConnectDisconnectEvents(deviceScope)
        }

    }

    private fun listenForIncomingPackets(deviceScope: CoroutineScope) {
        deviceScope.launch {
            for (message in pebbleDevice.appMessageService.receivedMessages) {
                when (message) {
                    is AppMessage.AppMessagePush -> {
                        lastReceivedMessage = AppMessageTimestamp(message.uuid.get(), Clock.System.now().toEpochMilliseconds())
                        platformAppMessageIPC.sendPush(message)
                    }

                    is AppMessage.AppMessageACK -> {
                        platformAppMessageIPC.sendAck(message)
                    }

                    is AppMessage.AppMessageNACK -> {
                        platformAppMessageIPC.sendNack(message.transactionId.get().toInt())
                    }
                }
            }
        }
    }

    private fun listenForOutgoingMessages(deviceScope: CoroutineScope) {
        outgoingMessages.buffer().onEach {
            when (it) {
                is OutgoingMessage.Data -> {
                    if (!isAppActive(it.uuid)) {
                        platformAppMessageIPC.sendNack(it.transactionId)
                    }
                    pebbleDevice.appMessageService.send(it.packet)
                }
                is OutgoingMessage.Ack -> {
                    pebbleDevice.appMessageService.send(it.packet)
                }
                is OutgoingMessage.Nack -> {
                    pebbleDevice.appMessageService.send(it.packet)
                }
                is OutgoingMessage.AppStart -> {
                    pebbleDevice.appRunStateService.startApp(it.uuid)
                }
                is OutgoingMessage.AppStop -> {
                    pebbleDevice.appRunStateService.stopApp(it.uuid)
                }
                is OutgoingMessage.AppCustomize -> {
                    pebbleDevice.appMessageService.send(
                        AppCustomizationSetStockAppTitleMessage(it.appType, it.name)
                    )
                }
            }
        }.launchIn(deviceScope)
    }

    private fun sendConnectDisconnectEvents(deviceScope: CoroutineScope) {
        deviceScope.launch {
            try {
                platformAppMessageIPC.broadcastPebbleConnected()
                awaitCancellation()
            } finally {
                platformAppMessageIPC.broadcastPebbleDisconnected()
            }
        }
    }

    private fun isAppActive(app: Uuid): Boolean {
        val lastReceivedMessage = lastReceivedMessage

        return if (pebbleDevice.currentActiveApp.value == app) {
            true
        } else if (lastReceivedMessage != null &&
                lastReceivedMessage.app == app &&
                (Clock.System.now().toEpochMilliseconds() - lastReceivedMessage.timestamp
                        ) < 5.seconds.inWholeMilliseconds) {
            // Sometimes app run state packets arrive with a delay. If we received incoming
            // AppMessage from the app within last 5 seconds, consider it active
            // and permit sending messages
            true
        } else {
            Logging.w("Invalid AppMessage intent. " +
                    "Wanted to send a message to the app $app, but ${pebbleDevice.currentActiveApp.value} is active on the watch.",
            )
            false
        }
    }
}