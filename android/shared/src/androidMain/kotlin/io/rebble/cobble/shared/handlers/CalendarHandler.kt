package io.rebble.cobble.shared.handlers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.datastore.KMPPrefs
import io.rebble.cobble.shared.domain.PermissionChangeBus
import io.rebble.cobble.shared.domain.calendar.CalendarSync
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.jobs.CalendarSyncWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val CALENDAR_WORK_TAG = "CalendarSync"

class CalendarHandler(private val pebbleDevice: PebbleDevice) : CobbleHandler, KoinComponent {
    private val calendarSync: CalendarSync by inject()
    private val prefs: KMPPrefs by inject()
    private val context: Context by inject()

    private var initialSyncJob: Job? = null
    private var calendarHandlerStarted = false

    private val calendarChangeFlow =
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 4,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    private val contentObserver =
        object : ContentObserver(null) {
            override fun onChange(
                selfChange: Boolean,
                uri: Uri?
            ) {
                if (!calendarChangeFlow.tryEmit(Unit)) {
                    Logging.e("Failed to emit calendar change event")
                }
            }
        }

    init {
        val permissionChangeFlow =
            PermissionChangeBus.permissionChangeFlow
                .onStart { emit(Unit) }

        combine(
            permissionChangeFlow,
            prefs.calendarSyncEnabled
        ) { _,
            calendarSyncEnabled ->
            startStopCalendar(calendarSyncEnabled)
        }.launchIn(pebbleDevice.negotiationScope)

        calendarChangeFlow
            .debounce(1000)
            .onEach {
                Timber.d("Calendar change detected. Syncing...")
                calendarSync.doFullCalendarSync()
            }
            .launchIn(pebbleDevice.negotiationScope)
    }

    private fun startStopCalendar(calendarSyncEnabled: Boolean) {
        val hasPermission =
            ContextCompat.checkSelfPermission(
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

        initialSyncJob =
            pebbleDevice.negotiationScope.launch {
                // We were not receiving any calendar changes when service was offline or we did not
                // have permissions.
                // Sync calendar

                Timber.d("Watch service started or we received permissions. Syncing calendar...")
                calendarSync.doFullCalendarSync()
                Timber.d("Sync complete")
            }

        pebbleDevice.negotiationScope.coroutineContext.job.invokeOnCompletion {
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
        val workRequest =
            PeriodicWorkRequestBuilder<CalendarSyncWorker>(
                repeatInterval = 24L,
                flexTimeInterval = 5L,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeIntervalUnit = TimeUnit.HOURS
            ).addTag(CALENDAR_WORK_TAG).build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private fun observeCalendarChanges() {
        context.contentResolver.registerContentObserver(
            CalendarContract.Instances.CONTENT_URI,
            true,
            contentObserver
        )
    }
}