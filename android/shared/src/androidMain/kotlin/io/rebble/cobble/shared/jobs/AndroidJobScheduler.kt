package io.rebble.cobble.shared.jobs

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.hours

class AndroidJobScheduler: KoinComponent {
    private val context: Context by inject()
    private val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

    companion object {
        private const val LOCKER_SYNC_JOB_ID = 1
        private val LOCKER_SYNC_JOB_PERIOD = 5.hours
    }
    fun scheduleStartupJobs() {
        scheduleLockerSyncPeriodic()
    }

    private fun buildLockerSyncJob() = JobInfo.Builder(LOCKER_SYNC_JOB_ID, ComponentName(context.applicationContext, AndroidLockerSyncJob::class.java))

    fun scheduleLockerSyncPeriodic() {
        val jobInfo = buildLockerSyncJob()
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(LOCKER_SYNC_JOB_PERIOD.inWholeMilliseconds)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            jobInfo.setPriority(JobInfo.PRIORITY_LOW)
        }

        jobScheduler.schedule(jobInfo.build())
    }

    fun scheduleLockerSync(userInitiated: Boolean) {
        val jobInfo = buildLockerSyncJob()
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        if (userInitiated) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                jobInfo.setUserInitiated(true)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                jobInfo.setPriority(JobInfo.PRIORITY_HIGH)
            }
        }
        jobScheduler.schedule(jobInfo.build())
    }
}