package io.rebble.cobble.shared.jobs

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import androidx.core.app.NotificationCompat
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.util.NotificationId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext

class AndroidLockerSyncJob(
        private val coroutineContext: CoroutineContext = Dispatchers.Default
): JobService(), KoinComponent {
    private lateinit var scope: CoroutineScope
    private val lockerSyncJob = LockerSyncJob()

    override fun onStartJob(params: JobParameters?): Boolean {
        require(params != null) { "Job parameters must not be null" }
        scope = CoroutineScope(coroutineContext + makeAndroidJobExceptionHandler(params, this::jobFinished))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && params.isUserInitiatedJob) {
            val notif = NotificationCompat.Builder(applicationContext, NotificationId.NOTIFICATION_CHANNEL_JOBS)
                    .setContentTitle("Syncing apps and watchfaces")
                    .setSmallIcon(android.R.drawable.ic_popup_sync)
                    .setOngoing(true)
                    .build()
            setNotification(params, NotificationId.LOCKER_SYNC, notif, JOB_END_NOTIFICATION_POLICY_REMOVE)
        }
        scope.launch {
            if (lockerSyncJob.beginSync()) {
                jobFinished(params, false)
            } else {
                Logging.i("Locker sync failed, rescheduling")
                jobFinished(params, true)
            }
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        require(params != null) { "Job parameters must not be null" }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            scope.cancel("Job stopped: ${params.stopReason}")
        } else {
            scope.cancel()
        }
        return true
    }
}