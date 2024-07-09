package io.rebble.cobble.di

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.rebble.cobble.errors.GlobalExceptionHandler
import io.rebble.cobble.shared.database.AppDatabase
import io.rebble.cobble.shared.database.dao.CachedPackageInfoDao
import io.rebble.cobble.shared.database.dao.PersistedNotificationDao
import io.rebble.cobble.shared.datastore.KMPPrefs
import io.rebble.cobble.shared.domain.calendar.CalendarSync
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus
import javax.inject.Singleton

@Module(
        subcomponents = [BackgroundFlutterSubcomponent::class]
)
abstract class AppModule {
    @Binds
    abstract fun bindContext(application: Application): Context

    @Binds
    abstract fun bindCoroutineExceptionHandler(
            globalExceptionHandler: GlobalExceptionHandler
    ): CoroutineExceptionHandler

    @Module
    companion object {
        @Provides
        fun providePackageManager(context: Context): PackageManager {
            return context.packageManager
        }
        @Provides
        @Singleton
        fun provideCalendarSync(exceptionHandler: CoroutineExceptionHandler, blobDBService: BlobDBService): CalendarSync {
            return CalendarSync(CoroutineScope(Dispatchers.Default) + exceptionHandler, blobDBService)
        }
        @Provides
        fun provideKMPPrefs(context: Context): KMPPrefs {
            return KMPPrefs()
        }
        @Provides
        fun providePersistedNotificationDao(context: Context): PersistedNotificationDao {
            return AppDatabase.instance().persistedNotificationDao()
        }
        @Provides
        fun provideCachedPackageInfoDao(context: Context): CachedPackageInfoDao {
            return AppDatabase.instance().cachedPackageInfoDao()
        }
    }
}