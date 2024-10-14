package io.rebble.cobble.shared.jobs

import android.app.job.JobParameters
import io.rebble.cobble.shared.Logging
import kotlinx.coroutines.CoroutineExceptionHandler

fun makeAndroidJobExceptionHandler(jobParams: JobParameters, jobFinished: (params: JobParameters, wantsReschedule: Boolean) -> Unit) = CoroutineExceptionHandler { _, throwable ->
    Logging.e("Job failed, will reschedule", throwable)
    jobFinished(jobParams, true)
}