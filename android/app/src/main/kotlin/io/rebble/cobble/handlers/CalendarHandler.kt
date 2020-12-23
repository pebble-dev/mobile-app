package io.rebble.cobble.handlers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.rebble.cobble.background.CalendarSyncWorker
import io.rebble.cobble.bridges.background.CalendarFlutterBridge
import io.rebble.cobble.datasources.FlutterPreferences
import io.rebble.cobble.datasources.PermissionChangeBus
import io.rebble.cobble.util.Debouncer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CalendarHandler @Inject constructor(
        private val context: Context,
        private val coroutineScope: CoroutineScope,
        private val calendarFlutterBridge: CalendarFlutterBridge,
        private val flutterPreferences: FlutterPreferences
) : PebbleMessageHandler {
    private var initialSyncJob: Job? = null
    private var calendarHandlerStarted = false

    val calendarDebouncer = Debouncer(debouncingTimeMs = 1_000, coroutineScope)

    private val contentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            // Android seems to fire multiple calendar notifications at the same time.
            // Use debouncer to wait for the last change and then trigger sync
            calendarDebouncer.executeDebouncing {
                Timber.d("Calendar change detected. Syncing...")
                calendarFlutterBridge.syncCalendar()
                Timber.d("Sync complete")
            }
        }
    }

    init {
        val permissionChangeFlow = PermissionChangeBus.openSubscription()
                .consumeAsFlow()
                .onStart { emit(Unit) }


        coroutineScope.launch {
            combine(
                    permissionChangeFlow,
                    flutterPreferences.calendarSyncEnabled
            ) { _,
                calendarSyncEnabled ->
                startStopCalendar(calendarSyncEnabled)
            }.collect()
        }
    }

    private fun startStopCalendar(calendarSyncEnabled: Boolean) {
        val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        val shouldSyncCalendar = hasPermission && calendarSyncEnabled

        if (shouldSyncCalendar && !calendarHandlerStarted) {
            startCalendarHandler()
        } else if (!shouldSyncCalendar && calendarHandlerStarted) {
            stopCalendarHandler()
        }
    }

    private fun startCalendarHandler() {
        observeCalendarChanges()
        schedulePeriodicCalendarSync()

        initialSyncJob = coroutineScope.launch {
            // We were not receiving any calendar changes when service was offline or we did not
            // have permissions.
            // Sync calendar

            Timber.d("Watch service started or we received permissions. Syncing calendar...")
            calendarFlutterBridge.syncCalendar()
            Timber.d("Sync complete")
        }

        coroutineScope.coroutineContext.job.invokeOnCompletion {
            stopCalendarHandler()
        }
    }

    private fun stopCalendarHandler() {
        initialSyncJob?.cancel()

        context.contentResolver.unregisterContentObserver(contentObserver)
        WorkManager.getInstance(context).cancelAllWorkByTag(CALENDAR_WORK_TAG)
    }

    private fun schedulePeriodicCalendarSync() {
        // Sync calendar approximately every 24 hours
        // (+- 5 hours to allow Android to batch tasks to save battery
        val workRequest = PeriodicWorkRequestBuilder<CalendarSyncWorker>(
                repeatInterval = 24L,
                flexTimeInterval = 5L,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeIntervalUnit = TimeUnit.HOURS
        ).addTag(CALENDAR_WORK_TAG).build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private fun observeCalendarChanges() {
        context.contentResolver.registerContentObserver(
                CalendarContract.Instances.CONTENT_URI, true, contentObserver
        )
    }

}

private val CALENDAR_WORK_TAG = "CalendarSync"