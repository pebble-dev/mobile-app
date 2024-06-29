package io.rebble.cobble.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import timber.log.Timber

class CalendarSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        Timber.d("Calendar sync worker start")
        val component = (applicationContext as CobbleApplication).component

        if (ConnectionStateManager.connectionState.value is ConnectionState.Disconnected) {
            // Periodic syncs are only needed when watch service is active
            // No need to do anything, sync will be performed when service starts

            Timber.d("Watch disconnected. Nothing to do...")

            return Result.success()
        }

        return if (component.createCalendarFlutterBridge().syncCalendar()) {
            Timber.d("Success")
            Result.success()
        } else {
            Timber.d("Flutter failure")
            Result.retry()
        }
    }
}