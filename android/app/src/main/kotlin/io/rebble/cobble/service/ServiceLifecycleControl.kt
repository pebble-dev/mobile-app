package io.rebble.cobble.service

import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import androidx.core.content.ContextCompat
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.shared.domain.notifications.NotificationListener
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.util.hasNotificationAccessPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ServiceLifecycleControl @Inject constructor(
        context: Context,
        connectionLooper: ConnectionLooper
) {
    private var serviceRunning = false
    private val serviceIntent = Intent(context, WatchService::class.java)

    init {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            connectionLooper.connectionState.collect {
                Timber.d("Watch connection status %s", it)

                val shouldServiceBeRunning = it !is ConnectionState.Disconnected

                if (shouldServiceBeRunning != serviceRunning) {
                    if (shouldServiceBeRunning) {
                        ContextCompat.startForegroundService(context, serviceIntent)
                    } else {
                        context.stopService(serviceIntent)
                    }

                    if (shouldServiceBeRunning &&
                            context.hasNotificationAccessPermission() && it !is ConnectionState.RecoveryMode) {
                        Timber.d("Requesting notifications rebind")
                        NotificationListenerService.requestRebind(
                                NotificationListener.getComponentName(context)
                        )
                    }

                    serviceRunning = shouldServiceBeRunning
                }

            }
        }
    }
}