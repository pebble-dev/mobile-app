package io.rebble.cobble.notifications

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import io.rebble.cobble.shared.database.AppDatabase
import io.rebble.cobble.shared.database.entity.CachedPackageInfo
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.hours

val StatusBarNotification.shouldDisplayGroupSummary: Boolean
    get() {
        // Check if the group is from a package that should not display group summaries
        return when (packageName) {
            "com.google.android.gm" -> false
            else -> true
        }
    }

private val cacheLifetime = 24.hours

suspend fun StatusBarNotification.queryPackage(context: Context): CachedPackageInfo? {
    val packageManager = context.packageManager
    val dao = AppDatabase.instance().cachedPackageInfoDao()
    val packageInfo = dao.getPackageInfo(packageName)
    return if (packageInfo == null || packageInfo.updated < (Clock.System.now() - cacheLifetime)) {
        val info = try {
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        } catch (e: NameNotFoundException) {
            return null
        }
        val cache = CachedPackageInfo(packageName, packageManager.getApplicationLabel(info).toString(), info.flags, Clock.System.now())
        dao.insert(cache)
        cache
    } else {
        packageInfo
    }
}