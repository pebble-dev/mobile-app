package io.rebble.cobble.bridges.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.pigeons.Pigeons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimelineSyncFlutterBridge @Inject constructor(
        private val context: Context,
        private val flutterBackgroundController: FlutterBackgroundController,
        private val connectionLooper: ConnectionLooper
) : FlutterBridge {
    private val alarmManager: AlarmManager = context.getSystemService()!!
    private var cachedTimelineSyncCallbacks: Pigeons.TimelineCallbacks? = null

    @Suppress("ObjectLiteralToLambda")
    private val callbacks = object : Pigeons.TimelineSyncControl {
        override fun syncTimelineToWatchLater() {
            Timber.d("Sync timeline to watch later")

            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30),
                    getPendingIntent()
            )
        }
    }

    init {
        GlobalScope.launch {
            val flutterEngine = flutterBackgroundController.getBackgroundFlutterEngine()
            if (flutterEngine != null) {
                Pigeons.TimelineSyncControl.setup(
                        flutterEngine.dartExecutor.binaryMessenger,
                        callbacks
                )
            }
        }

        GlobalScope.launch(Dispatchers.Main.immediate) {
            connectionLooper.connectionState.collect {
                if (it !is ConnectionState.Connected) {
                    // Watch has disconnected.
                    // No need to perform delayed sync anymore. Sync will be automatically
                    // performed when watch reconnects.
                    alarmManager.cancel(getPendingIntent())
                }
            }
        }
    }

    fun syncTimelineToWatchNow() {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            getTimelineSyncCallbacks()?.syncTimelineToWatch { }
        }
    }

    private suspend fun getTimelineSyncCallbacks(): Pigeons.TimelineCallbacks? {
        val cachedTimelineSyncCallbacks = cachedTimelineSyncCallbacks
        if (cachedTimelineSyncCallbacks != null) {
            return cachedTimelineSyncCallbacks
        }

        val flutterEngine = flutterBackgroundController.getBackgroundFlutterEngine() ?: return null
        return Pigeons.TimelineCallbacks(flutterEngine.dartExecutor.binaryMessenger)
                .also { this.cachedTimelineSyncCallbacks = it }
    }

    private fun getPendingIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
                context,
                998,
                Intent(context, Receiver::class.java),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
    }

    class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            Timber.d("Delayed sync broadcast received")

            val component = (context.applicationContext as CobbleApplication).component

            component.createTimelineSyncFlutterBridge().syncTimelineToWatchNow()
        }
    }
}