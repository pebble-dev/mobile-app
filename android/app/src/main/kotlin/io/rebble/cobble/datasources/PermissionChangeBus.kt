package io.rebble.cobble.datasources

import android.content.Context
import io.rebble.cobble.util.hasNotificationAccessPermission
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*

/**
 * Bus that triggers whenever permissions change
 */
@OptIn(ExperimentalCoroutinesApi::class)
object PermissionChangeBus {
    private val permissionChangeChannel = BroadcastChannel<Unit>(Channel.CONFLATED)

    fun openSubscription(): ReceiveChannel<Unit> {
        return permissionChangeChannel.openSubscription()
    }

    fun trigger() {
        permissionChangeChannel.trySend(Unit).getOrThrow()
    }
}

fun PermissionChangeBus.notificationPermissionFlow(context: Context): Flow<Boolean> {
    return (openSubscription().consumeAsFlow().onStart { emit(Unit) })
            .map { context.hasNotificationAccessPermission() }
            .distinctUntilChanged()
}