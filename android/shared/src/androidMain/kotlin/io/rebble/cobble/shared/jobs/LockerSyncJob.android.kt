package io.rebble.cobble.shared.jobs

import io.rebble.cobble.shared.PlatformContext
import org.koin.mp.KoinPlatformTools

actual fun scheduleLockerSyncJob(context: PlatformContext) {
    val androidJobScheduler: AndroidJobScheduler = KoinPlatformTools.defaultContext().get().get()
    androidJobScheduler.scheduleLockerSync(true)
}