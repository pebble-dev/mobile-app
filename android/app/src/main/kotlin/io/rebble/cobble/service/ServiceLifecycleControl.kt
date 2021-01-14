package io.rebble.cobble.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
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

                    serviceRunning = shouldServiceBeRunning
                }

            }
        }
    }
}