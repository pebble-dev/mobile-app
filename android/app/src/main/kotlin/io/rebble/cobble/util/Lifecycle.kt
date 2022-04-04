package io.rebble.cobble.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@OptIn(ExperimentalCoroutinesApi::class)
fun Lifecycle.asFlow(): Flow<Lifecycle.State> {
    return callbackFlow {
        trySend(currentState).getOrThrow()

        val observer = LifecycleEventObserver { _, _ ->
            trySend(currentState).getOrThrow()
        }

        addObserver(observer)

        awaitClose {
            removeObserver(observer)
        }
    }
}