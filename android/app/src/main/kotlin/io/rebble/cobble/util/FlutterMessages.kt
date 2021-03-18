package io.rebble.cobble.util

import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.rebble.cobble.pigeons.Pigeons
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun BasicMessageChannel<Any>.setCoroutineMessageHandler(
        coroutineScope: CoroutineScope,
        block: suspend (Any?) -> Any?
) {
    setMessageHandler { message, reply ->
        // Replies must be sent through UI thread
        coroutineScope.launch(Dispatchers.Main.immediate) {
            reply.reply(block(message))
        }
    }
}

/**
 * Regular pigeon does not support async methods
 * This workaround is used to make calls manually until
 * https://github.com/flutter/flutter/issues/60399 is resolved
 *
 * @param callName Name of the pigeon call. Copy from *Pigeons.java*.
 */
@Deprecated("Use native pigeon's support for @async and wrap it with launchPigeonResult")
fun BinaryMessenger.registerAsyncPigeonCallback(
        coroutineScope: CoroutineScope,
        callName: String,
        callback: suspend (rawMessage: HashMap<*, *>?) -> Map<*, *>
) {
    val channel = BasicMessageChannel(
            this,
            callName,
            StandardMessageCodec()
    )
    channel.setCoroutineMessageHandler(coroutineScope) { rawMessage ->
        @Suppress("UNCHECKED_CAST")
        val result = callback(rawMessage as HashMap<*, *>?)

        hashMapOf("result" to result)
    }

    coroutineScope.coroutineContext[Job]?.invokeOnCompletion {
        channel.setMessageHandler(null)
    }
}

val voidResult: Map<*, *> = mapOf("result" to null)

fun <T> CoroutineScope.launchPigeonResult(result: Pigeons.Result<T>,
                                          coroutineContext: CoroutineContext = EmptyCoroutineContext,
                                          callback: suspend () -> T) {
    launch(coroutineContext) {
        val callbackResult = callback()
        withContext(Dispatchers.Main.immediate) {
            result.success(callbackResult)
        }
    }
}

suspend fun <T> awaitPigeonMethod(block: (reply: (T) -> Unit) -> Unit): T {
    return suspendCoroutine { continuation ->
        block(continuation::resume)
    }
}