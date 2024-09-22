package io.rebble.cobble.shared.jobs

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.rebble.cobble.shared.domain.calendar.CalendarSync
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class CalendarSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params), KoinComponent {
    private val calendarSync: CalendarSync by inject()
    override suspend fun doWork(): Result {
        Timber.d("Calendar sync worker start")

        if (ConnectionStateManager.connectionState.value is ConnectionState.Disconnected) {
            // Periodic syncs are only needed when watch service is active
            // No need to do anything, sync will be performed when service starts

            Timber.d("Watch disconnected. Nothing to do...")

            return Result.success()
        }

        return if (calendarSync.doFullCalendarSync()) {
            Timber.d("Success")
            Result.success()
        } else {
            Timber.d("KMP failure")
            Result.retry()
        }
    }
}