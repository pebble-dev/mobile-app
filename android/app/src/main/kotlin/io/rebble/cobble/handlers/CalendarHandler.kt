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
import io.rebble.cobble.datasources.FlutterPreferences
import io.rebble.cobble.datasources.PermissionChangeBus
import io.rebble.cobble.shared.datastore.KMPPrefs
import io.rebble.cobble.shared.domain.calendar.CalendarSync
import io.rebble.cobble.util.Debouncer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CalendarHandler @Inject constructor(
        private val context: Context,
        private val coroutineScope: CoroutineScope,
        private val calendarSync: CalendarSync,
        private val flutterPreferences: FlutterPreferences,
        private val prefs: KMPPrefs
) : CobbleHandler {
    private var initialSyncJob: Job? = null
    private var calendarHandlerStarted = false

    val calendarDebouncer = Debouncer(debouncingTimeMs = 1_000, scope = coroutineScope)

    private val contentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            // Android seems to fire multiple calendar notifications at the same time.
            // Use debouncer to wait for the last change and then trigger sync
            calendarDebouncer.executeDebouncing {
                Timber.d("Calendar change detected. Syncing...")
                calendarSync.doFullCalendarSync()
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
                    prefs.calendarSyncEnabled
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
        synchronized(this) {
            val shouldSyncCalendar = hasPermission && calendarSyncEnabled
            Timber.d("Should sync calendar: $shouldSyncCalendar")

            if (shouldSyncCalendar && !calendarHandlerStarted) {
                startCalendarHandler()
                calendarHandlerStarted = true
            } else if (!shouldSyncCalendar && calendarHandlerStarted) {
                stopCalendarHandler()
                calendarHandlerStarted = false
            }
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
            calendarSync.doFullCalendarSync()
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

private const val CALENDAR_WORK_TAG = "CalendarSync"