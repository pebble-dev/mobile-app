package io.rebble.cobble.shared.handlers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.benasher44.uuid.uuidFrom
import com.getpebble.android.kit.Constants
import com.getpebble.android.kit.util.PebbleDictionary
import io.rebble.cobble.shared.util.coroutines.asFlow
import io.rebble.cobble.shared.util.getIntExtraOrNull
import io.rebble.cobble.shared.util.getPebbleDictionary
import io.rebble.cobble.shared.util.toPacket
import io.rebble.libpebblecommon.packets.AppMessage
import io.rebble.libpebblecommon.packets.AppType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class AndroidPlatformAppMessageIPC(private val context: Context) : PlatformAppMessageIPC {
    override fun sendPush(message: AppMessage.AppMessagePush) {
        val intent =
            Intent(Constants.INTENT_APP_RECEIVE).apply {
                putExtra(Constants.APP_UUID, message.uuid.get())
                putExtra(Constants.TRANSACTION_ID, message.transactionId.get().toInt())

                val dictionary = message.getPebbleDictionary()
                putExtra(Constants.MSG_DATA, dictionary.toJsonString())
            }
        context.sendBroadcast(intent)
    }

    override fun sendAck(message: AppMessage.AppMessageACK) {
        val intent =
            Intent(Constants.INTENT_APP_RECEIVE_ACK).apply {
                putExtra(Constants.TRANSACTION_ID, message.transactionId.get().toInt())
            }

        context.sendBroadcast(intent)
    }

    override fun sendNack(transactionId: Int) {
        val intent =
            Intent(Constants.INTENT_APP_RECEIVE_NACK).apply {
                putExtra(Constants.TRANSACTION_ID, transactionId)
            }

        context.sendBroadcast(intent)
    }

    override fun outgoingMessages(): Flow<OutgoingMessage> {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.INTENT_APP_SEND)
        intentFilter.addAction(Constants.INTENT_APP_ACK)
        intentFilter.addAction(Constants.INTENT_APP_NACK)
        intentFilter.addAction(Constants.INTENT_APP_START)
        intentFilter.addAction(Constants.INTENT_APP_STOP)
        intentFilter.addAction(Constants.INTENT_APP_CUSTOMIZE)
        return intentFilter.asFlow(context).mapNotNull {
            when (it.action) {
                Constants.INTENT_APP_SEND -> {
                    val uuid = uuidFrom(it.getStringExtra(Constants.APP_UUID)!!)
                    val transactionId = it.getIntExtra(Constants.TRANSACTION_ID, 0)
                    val dictionary =
                        PebbleDictionary.fromJson(
                            it.getStringExtra(Constants.MSG_DATA)!!
                        )

                    OutgoingMessage.Data(
                        uuid,
                        transactionId,
                        dictionary.toPacket(uuid, transactionId)
                    )
                }
                Constants.INTENT_APP_ACK -> {
                    val transactionId: Int = it.getIntExtra(Constants.TRANSACTION_ID, 0)

                    OutgoingMessage.Ack(
                        AppMessage.AppMessageACK(
                            transactionId.toUByte()
                        )
                    )
                }
                Constants.INTENT_APP_NACK -> {
                    val transactionId: Int = it.getIntExtra(Constants.TRANSACTION_ID, 0)

                    OutgoingMessage.Nack(
                        AppMessage.AppMessageNACK(
                            transactionId.toUByte()
                        )
                    )
                }
                Constants.INTENT_APP_START -> {
                    val uuid = uuidFrom(it.getStringExtra(Constants.APP_UUID)!!)

                    OutgoingMessage.AppStart(uuid)
                }
                Constants.INTENT_APP_STOP -> {
                    val uuid = uuidFrom(it.getStringExtra(Constants.APP_UUID)!!)

                    OutgoingMessage.AppStop(uuid)
                }
                Constants.INTENT_APP_CUSTOMIZE -> {
                    val appType =
                        it.getIntExtraOrNull(Constants.CUST_APP_TYPE)
                            ?.let { type ->
                                if (type == 0) {
                                    AppType.SPORTS
                                } else {
                                    AppType.GOLF
                                }
                            }
                            ?: return@mapNotNull null

                    val name = it.getStringExtra(Constants.CUST_NAME) ?: return@mapNotNull null

                    OutgoingMessage.AppCustomize(
                        appType,
                        name
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
                else -> throw IllegalStateException("Unknown action: ${it.action}")
            }
        }
    }

    override fun broadcastPebbleConnected() {
        context.sendBroadcast(Intent(Constants.INTENT_PEBBLE_CONNECTED))
    }

    override fun broadcastPebbleDisconnected() {
        context.sendBroadcast(Intent(Constants.INTENT_PEBBLE_DISCONNECTED))
    }
}