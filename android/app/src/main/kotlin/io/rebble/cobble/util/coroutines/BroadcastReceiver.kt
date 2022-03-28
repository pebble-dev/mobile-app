package io.rebble.cobble.util.coroutines

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Consume intents from specific IntentFilter as coroutine flow
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun IntentFilter.asFlow(context: Context): Flow<Intent> = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            trySend(intent).getOrThrow()
        }
    }

    context.registerReceiver(receiver, this@asFlow)

    awaitClose {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            // unregisterReceiver can throw IllegalArgumentException if receiver
            // was already unregistered
            // This is not a problem, we can eat the exception
        }

    }
}