package io.rebble.cobble.di

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.service.notification.StatusBarNotification
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.rebble.cobble.shared.database.AppDatabase
import io.rebble.cobble.shared.database.dao.CachedPackageInfoDao
import io.rebble.cobble.shared.database.dao.NotificationChannelDao
import io.rebble.cobble.shared.database.dao.PersistedNotificationDao
import io.rebble.cobble.shared.datastore.FlutterPreferences
import io.rebble.cobble.shared.datastore.KMPPrefs
import io.rebble.cobble.shared.datastore.SecureStorage
import io.rebble.cobble.shared.domain.calendar.CalendarSync
import io.rebble.cobble.shared.domain.state.CurrentToken
import io.rebble.cobble.shared.errors.GlobalExceptionHandler
import io.rebble.cobble.shared.jobs.AndroidJobScheduler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.plus
import org.koin.core.qualifier.named
import org.koin.mp.KoinPlatformTools
import java.util.UUID
import javax.inject.Singleton

@Module
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
        fun provideCalendarSync(exceptionHandler: CoroutineExceptionHandler): CalendarSync {
            return CalendarSync(CoroutineScope(Dispatchers.Default) + exceptionHandler)
        }

        @Provides
        fun provideKMPPrefs(context: Context): KMPPrefs {
            return KMPPrefs()
        }

        @Provides
        fun provideGlobalExceptionHandler(): GlobalExceptionHandler {
            return KoinPlatformTools.defaultContext().get().get()
        }

        @Provides
        fun provideTokenState(): MutableStateFlow<CurrentToken> {
            return KoinPlatformTools.defaultContext().get().get(named("currentToken"))
        }

        @Provides
        fun provideSecureStorage(): SecureStorage {
            return KoinPlatformTools.defaultContext().get().get()
        }

        @Provides
        fun providePersistedNotificationDao(context: Context): PersistedNotificationDao {
            return AppDatabase.instance().persistedNotificationDao()
        }

        @Provides
        fun provideCachedPackageInfoDao(context: Context): CachedPackageInfoDao {
            return AppDatabase.instance().cachedPackageInfoDao()
        }

        @Provides
        fun provideNotificationChannelDao(context: Context): NotificationChannelDao {
            return AppDatabase.instance().notificationChannelDao()
        }

        @Provides
        @Singleton
        fun proviceAndroidJobScheduler(context: Context): AndroidJobScheduler {
            return KoinPlatformTools.defaultContext().get().get()
        }

        @Provides
        @Singleton
        fun provideActiveNotifsState(): MutableStateFlow<Map<UUID, StatusBarNotification>> {
            return KoinPlatformTools.defaultContext().get().get(named("activeNotifsState"))
        }

        @Provides
        @Singleton
        fun provideFlutterPreferences(): FlutterPreferences {
            return KoinPlatformTools.defaultContext().get().get()
        }
    }
}