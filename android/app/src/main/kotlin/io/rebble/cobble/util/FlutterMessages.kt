package io.rebble.cobble.util

import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

fun BasicMessageChannel<Any>.setCoroutineMessageHandler(
        coroutineScope: CoroutineScope,
        block: suspend (Any?) -> Any?
) {
    setMessageHandler { message, reply ->
        coroutineScope.launch {
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
fun BinaryMessenger.registerAsyncPigeonCallback(
        coroutineScope: CoroutineScope,
        callName: String,
        callback: suspend () -> Map<*, *>
) {
    val channel = BasicMessageChannel(
            this,
            callName,
            StandardMessageCodec()
    )
    channel.setCoroutineMessageHandler(coroutineScope) { _ ->
        val result = callback()

        hashMapOf("result" to result)
    }

    coroutineScope.coroutineContext.job.invokeOnCompletion {
        channel.setMessageHandler(null)
    }
}