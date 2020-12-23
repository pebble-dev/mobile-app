package io.rebble.cobble.datasources

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

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
        permissionChangeChannel.offer(Unit)
    }
}