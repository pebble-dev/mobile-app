package io.rebble.cobble.handlers

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.getpebble.android.kit.Constants
import com.getpebble.android.kit.util.PebbleDictionary
import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.cobble.middleware.getPebbleDictionary
import io.rebble.cobble.middleware.toPacket
import io.rebble.cobble.shared.handlers.CobbleHandler
import io.rebble.cobble.util.coroutines.asFlow
import io.rebble.cobble.util.getIntExtraOrNull
import io.rebble.libpebblecommon.packets.AppCustomizationSetStockAppTitleMessage
import io.rebble.libpebblecommon.packets.AppMessage
import io.rebble.libpebblecommon.packets.AppRunStateMessage
import io.rebble.libpebblecommon.packets.AppType
import io.rebble.libpebblecommon.services.app.AppRunStateService
import io.rebble.libpebblecommon.services.appmessage.AppMessageService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
@SuppressLint("BinaryOperationInTimber")
class AppMessageHandler @Inject constructor(
        private val context: Context,
        private val appMessageService: AppMessageService,
        private val appRunStateService: AppRunStateService,
        private val coroutineScope: CoroutineScope,
        private val watchMetadataStore: WatchMetadataStore
) : CobbleHandler {
    private var lastReceivedMessage: AppMessageTimestamp? = null

    init {
        listenForIncomingPackets()

        listenForOutgoingDataMessages()
        listenForOutgoingAckMessages()
        listenForOutgoingNackMessages()

        listenForOutgoingAppStartMessages()
        listenForOutgoingAppStopMessages()

        listenForOutgoingAppCustomizeMessages()

        sendConnectDisconnectIntents()
    }

    private fun sendPushIntent(message: AppMessage.AppMessagePush) {
        lastReceivedMessage = AppMessageTimestamp(message.uuid.get(), System.currentTimeMillis())

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

    private fun sendNackIntent(transactionId: Int) {
        val intent = Intent(Constants.INTENT_APP_RECEIVE_NACK).apply {
            putExtra(Constants.TRANSACTION_ID, transactionId)
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
                        sendNackIntent(message.transactionId.get().toInt())
                    }
                }
            }
        }
    }

    private fun listenForOutgoingDataMessages() {
        coroutineScope.launch {
            IntentFilter(Constants.INTENT_APP_SEND).asFlow(context).collect { intent ->
                val uuid = intent.getSerializableExtra(Constants.APP_UUID) as UUID
                val transactionId: Int = intent.getIntExtra(Constants.TRANSACTION_ID, 0)

                if (!isAppActive(uuid)) {
                    sendNackIntent(transactionId)
                    return@collect
                }

                val dictionary: PebbleDictionary = PebbleDictionary.fromJson(
                        intent.getStringExtra(Constants.MSG_DATA)
                )

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

    private fun listenForOutgoingAppCustomizeMessages() {
        coroutineScope.launch {
            IntentFilter(Constants.INTENT_APP_CUSTOMIZE).asFlow(context).collect { intent ->
                val appType = intent.getIntExtraOrNull(Constants.CUST_APP_TYPE)
                        ?.let {
                            if (it == 0) {
                                AppType.SPORTS
                            } else {
                                AppType.GOLF
                            }
                        }
                        ?: return@collect

                val name = intent.getStringExtra(Constants.CUST_NAME) ?: return@collect

                appMessageService.send(
                        AppCustomizationSetStockAppTitleMessage(appType, name)
                )

                // Pebble watch is also supposed to support customizing the icon of the
                // sports/golf app, but this does not appear to work, even with the stock app
                // maybe it was removed during later firmware upgrades?

                // Packets are there, but they do not work. Let's comment this until/if we
                // ever figure this one out or if RebbleOS fixes it.

//                val image = intent.getParcelableExtra<Bitmap>(Constants.CUST_ICON) ?: return@collect
//
//                appMessageService.send(
//                        AppCustomizationSetStockAppIconMessage(
//                                appType,
//                                io.rebble.libpebblecommon.util.Bitmap(image)
//                        )
//                )
            }
        }
    }

    private fun sendConnectDisconnectIntents() {
        coroutineScope.launch {
            try {
                context.sendBroadcast(Intent(Constants.INTENT_PEBBLE_CONNECTED))
                awaitCancellation()
            } finally {
                context.sendBroadcast(Intent(Constants.INTENT_PEBBLE_DISCONNECTED))
            }
        }
    }

    private fun isAppActive(app: UUID): Boolean {
        val lastReceivedMessage = lastReceivedMessage

        return if (watchMetadataStore.currentActiveApp.value == app) {
            true
        } else if (lastReceivedMessage != null &&
                lastReceivedMessage.app == app &&
                (System.currentTimeMillis() - lastReceivedMessage.timestamp
                        ) < TimeUnit.SECONDS.toMillis(5)) {
            // Sometimes app run state packets arrive with a delay. If we received incoming
            // AppMessage from the app within last 5 seconds, consider it active
            // and permit sending messages
            true
        } else {
            Timber.w("Invalid AppMessage intent. " +
                    "Wanted to send a message to the app %s, but %s is active on the watch.",
                    app,
                    watchMetadataStore.currentActiveApp.value
            )

            false
        }
    }
}

private data class AppMessageTimestamp(val app: UUID, val timestamp: Long)