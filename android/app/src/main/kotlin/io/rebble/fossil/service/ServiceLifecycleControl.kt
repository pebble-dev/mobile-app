package io.rebble.fossil.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import io.rebble.fossil.WatchService
import io.rebble.fossil.bluetooth.ConnectionLooper
import io.rebble.fossil.bluetooth.ConnectionState
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
        GlobalScope.launch(Dispatchers.Main) {
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