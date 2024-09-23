package io.rebble.cobble.shared.domain

import android.content.Context
import io.rebble.cobble.shared.util.hasNotificationAccessPermission
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/**
 * Bus that triggers whenever permissions change
 */
object PermissionChangeBus {
    private val _permissionChangeFlow = MutableSharedFlow<Unit>()
    val permissionChangeFlow = _permissionChangeFlow.asSharedFlow()

    fun trigger() {
        _permissionChangeFlow.tryEmit(Unit)
    }
}

fun PermissionChangeBus.notificationPermissionFlow(context: Context): Flow<Boolean> {
    return (permissionChangeFlow.onStart { emit(Unit) })
            .map { context.hasNotificationAccessPermission() }
            .distinctUntilChanged()
}