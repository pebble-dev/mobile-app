package io.rebble.fossil.handlers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.getpebble.android.kit.Constants
import com.getpebble.android.kit.util.PebbleDictionary
import io.rebble.fossil.di.PerService
import io.rebble.fossil.middleware.getPebbleDictionary
import io.rebble.fossil.middleware.toPacket
import io.rebble.fossil.util.coroutines.asFlow
import io.rebble.libpebblecommon.packets.AppMessage
import io.rebble.libpebblecommon.packets.AppRunStateMessage
import io.rebble.libpebblecommon.services.app.AppRunStateService
import io.rebble.libpebblecommon.services.appmessage.AppMessageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
@PerService
class AppMessageHandler @Inject constructor(
        private val context: Context,
        private val appMessageService: AppMessageService,
        private val appRunStateService: AppRunStateService,
        private val coroutineScope: CoroutineScope
) : PebbleMessageHandler {
    init {
        listenForIncomingPackets()

        listenForOutgoingDataMessages()
        listenForOutgoingAckMessages()
        listenForOutgoingNackMessages()

        listenForOutgoingAppStartMessages()
        listenForOutgoingAppStopMessages()
    }

    private fun sendPushIntent(message: AppMessage.AppMessagePush) {
        val intent = Intent(Constants.INTENT_APP_RECEIVE).apply {
            putExtra(Constants.APP_UUID, message.uuid.get())
            putExtra(Constants.TRANSACTION_ID, message.transactionId.get().toInt())

            val dictionary = message.getPebbleDictionary()
            putExtra(Constants.MSG_DATA, dictionary.toJsonString())
        }
        context.sendBroadcast(intent)
    }

    private fun sendAckIntent(message: AppMessage.AppMessageACK) {
        val intent = Intent(Constants.INTENT_APP_RECEIVE_ACK).apply {
            putExtra(Constants.TRANSACTION_ID, message.transactionId.get().toInt())
        }

        context.sendBroadcast(intent)
    }

    private fun sendNackIntent(message: AppMessage.AppMessageNACK) {
        val intent = Intent(Constants.INTENT_APP_RECEIVE_NACK).apply {
            putExtra(Constants.TRANSACTION_ID, message.transactionId.get().toInt())
        }

        context.sendBroadcast(intent)
    }

    private fun listenForIncomingPackets() {
        coroutineScope.launch {
            for (message in appMessageService.receivedMessages) {
                when (message) {
                    is AppMessage.AppMessagePush -> {
                        sendPushIntent(message)
                    }
                    is AppMessage.AppMessageACK -> {
                        sendAckIntent(message)
                    }
                    is AppMessage.AppMessageNACK -> {
                        sendNackIntent(message)
                    }
                }
            }
        }
    }

    private fun listenForOutgoingDataMessages() {
        coroutineScope.launch {
            IntentFilter(Constants.INTENT_APP_SEND).asFlow(context).collect { intent ->
                val uuid = intent.getSerializableExtra(Constants.APP_UUID) as UUID
                val dictionary: PebbleDictionary = PebbleDictionary.fromJson(
                        intent.getStringExtra(Constants.MSG_DATA)
                )
                val transactionId: Int = intent.getIntExtra(Constants.TRANSACTION_ID, 0)

                val packet = dictionary.toPacket(uuid, transactionId)

                appMessageService.send(packet)
            }
        }
    }

    private fun listenForOutgoingAckMessages() {
        coroutineScope.launch {
            IntentFilter(Constants.INTENT_APP_ACK).asFlow(context).collect { intent ->

                val transactionId: Int = intent.getIntExtra(Constants.TRANSACTION_ID, 0)

                val packet = AppMessage.AppMessageACK(
                        transactionId.toUByte()
                )

                appMessageService.send(packet)
            }

        }
    }

    private fun listenForOutgoingNackMessages() {
        coroutineScope.launch {
            IntentFilter(Constants.INTENT_APP_NACK).asFlow(context).collect { intent ->
                val transactionId: Int = intent.getIntExtra(Constants.TRANSACTION_ID, 0)

                val packet = AppMessage.AppMessageNACK(
                        transactionId.toUByte()
                )

                appMessageService.send(packet)
            }
        }
    }

    private fun listenForOutgoingAppStartMessages() {
        coroutineScope.launch {
            IntentFilter(Constants.INTENT_APP_START).asFlow(context).collect { intent ->
                val uuid = intent.getSerializableExtra(Constants.APP_UUID) as UUID
                val packet = AppRunStateMessage.AppRunStateStart(uuid)
                appRunStateService.send(packet)
            }
        }
    }

    private fun listenForOutgoingAppStopMessages() {
        coroutineScope.launch {
            IntentFilter(Constants.INTENT_APP_STOP).asFlow(context).collect { intent ->
                val uuid = intent.getSerializableExtra(Constants.APP_UUID) as UUID
                val packet = AppRunStateMessage.AppRunStateStop(uuid)
                appRunStateService.send(packet)
            }
        }
    }
}