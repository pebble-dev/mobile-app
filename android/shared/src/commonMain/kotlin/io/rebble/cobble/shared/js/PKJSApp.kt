package io.rebble.cobble.shared.js

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.handlers.OutgoingMessage
import io.rebble.cobble.shared.handlers.getAppPbwFile
import io.rebble.cobble.shared.util.getPbwJsFilePath
import io.rebble.cobble.shared.util.requirePbwAppInfo
import io.rebble.cobble.shared.util.requirePbwJsFilePath
import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import io.rebble.libpebblecommon.packets.AppMessage
import io.rebble.libpebblecommon.packets.AppMessageTuple
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PKJSApp(val uuid: Uuid): KoinComponent {
    private val context: PlatformContext by inject()
    private val pbw = getAppPbwFile(context, uuid.toString())
    private val appInfo = requirePbwAppInfo(pbw)
    private val jsPath = requirePbwJsFilePath(context, appInfo, pbw)
    private var jsRunner: JsRunner? = null
    private var runningScope: CoroutineScope? = null

    companion object {
        fun isJsApp(context: PlatformContext, uuid: Uuid): Boolean {
            val pbw = getAppPbwFile(context, uuid.toString())
            return pbw.exists() && getPbwJsFilePath(context, requirePbwAppInfo(pbw), pbw) != null
        }
    }

    suspend fun start(device: PebbleDevice) {
        val connectionScope = withTimeout(1000) {
            device.connectionScope.filterNotNull().first()
        }
        val scope = connectionScope + SupervisorJob() + CoroutineName("PKJSApp-$uuid")
        runningScope = scope
        jsRunner = JsRunnerFactory.createJsRunner(scope, device, appInfo, jsPath)
        device.incomingAppMessages.onEach {
            if (it is AppMessage.AppMessagePush && it.uuid.get() != uuid) {
                Logging.v("Ignoring app message for different app: ${it.uuid.get()} != $uuid")
                return@onEach
            }
            Logging.d("Received app message: $it")
            withTimeout(1000) {
                device.outgoingAppMessages.emit(OutgoingMessage.Ack(AppMessage.AppMessageACK(it.transactionId.get())))
            }
            when (it) {
                is AppMessage.AppMessagePush -> jsRunner?.signalNewAppMessageData(it.dictionary.list.toJSDataString(appInfo.appKeys))
                is AppMessage.AppMessageACK -> jsRunner?.signalAppMessageAck(it.toJSDataString())
                is AppMessage.AppMessageNACK -> jsRunner?.signalAppMessageNack(it.toJSDataString())
            }
        }.catch {
            Logging.e("Error receiving app message", it)
        }.launchIn(scope)
        jsRunner?.outgoingAppMessages?.onEach {
            Logging.d("Sending app message: $it")
            val appMessage = AppMessage.fromJSDataString(it, appInfo)
            val tID = device.appMessageTransactionSequence.next()
            appMessage.transactionId.set(tID)
            device.appMessageService.send(appMessage)
        }?.catch {
            Logging.e("Error sending app message", it)
        }?.launchIn(scope) ?: error("JsRunner not initialized")
        jsRunner?.start()
    }

    suspend fun stop() {
        jsRunner?.stop()
        runningScope?.cancel()
        jsRunner = null
    }
}

fun List<AppMessageTuple>.toJSDataString(appKeys: Map<String, Int>): String {
    val obj = buildJsonObject {
        for (tuple in this@toJSDataString) {
            val type = tuple.type.get()
            val keyId = tuple.key.get()
            val key = appKeys.entries.firstOrNull { it.value.toUInt() == keyId }?.key ?: keyId.toString()

            when (type) {
                AppMessageTuple.Type.ByteArray.value -> {
                    val array = buildJsonArray {
                        for (byte in tuple.dataAsBytes) {
                            add(byte.toInt())
                        }
                    }
                    put(key, array)
                }
                AppMessageTuple.Type.CString.value -> {
                    put(key, tuple.dataAsString)
                }
                AppMessageTuple.Type.UInt.value -> {
                    put(key, tuple.dataAsUnsignedNumber)
                }
                AppMessageTuple.Type.Int.value -> {
                    put(key, tuple.dataAsSignedNumber)
                }
            }
        }
    }
    return Json.encodeToString(obj)
}

fun AppMessage.AppMessageNACK.toJSDataString(): String {
    return buildJsonObject {
        put("transactionId", transactionId.get().toInt())
    }.toString()
}

fun AppMessage.AppMessageACK.toJSDataString(): String {
    return buildJsonObject {
        put("transactionId", transactionId.get().toInt())
    }.toString()
}

fun AppMessage.Companion.fromJSDataString(json: String, appInfo: PbwAppInfo): AppMessage {
    val jsonElement = Json.parseToJsonElement(json)
    val jsonObject = jsonElement.jsonObject
    val tuples = jsonObject.mapNotNull { objectEntry ->
        val key = objectEntry.key
        val keyId = appInfo.appKeys[key] ?: return@mapNotNull null
        when (objectEntry.value) {
            is JsonArray -> {
                AppMessageTuple.createUByteArray(keyId.toUInt(), objectEntry.value.jsonArray.map { it.jsonPrimitive.long.toUByte() }.toUByteArray())
            }
            is JsonObject -> error("Invalid JSON value, JsonObject not supported")
            else -> {
                when {
                    objectEntry.value.jsonPrimitive.isString -> {
                        AppMessageTuple.createString(keyId.toUInt(), objectEntry.value.jsonPrimitive.content)
                    }
                    objectEntry.value.jsonPrimitive.intOrNull != null -> {
                        AppMessageTuple.createInt(keyId.toUInt(), objectEntry.value.jsonPrimitive.long.toInt())
                    }
                    objectEntry.value.jsonPrimitive.longOrNull != null -> {
                        AppMessageTuple.createUInt(keyId.toUInt(), objectEntry.value.jsonPrimitive.long.toUInt())
                    }
                    objectEntry.value.jsonPrimitive.booleanOrNull != null -> {
                        AppMessageTuple.createShort(keyId.toUInt(), if (objectEntry.value.jsonPrimitive.boolean) 1.toShort() else 0.toShort())
                    }
                    else -> error("Invalid JSON value, unsupported primitive type")
                }
            }
        }
    }
    return AppMessage.AppMessagePush(uuid = uuidFrom(appInfo.uuid), tuples = tuples)
}