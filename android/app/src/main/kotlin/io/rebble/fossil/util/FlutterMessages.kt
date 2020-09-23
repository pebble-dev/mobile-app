package io.rebble.fossil.util

import io.flutter.plugin.common.BasicMessageChannel
import kotlinx.coroutines.CoroutineScope
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