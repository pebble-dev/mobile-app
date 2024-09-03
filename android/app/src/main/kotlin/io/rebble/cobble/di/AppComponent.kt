package io.rebble.cobble.di

import android.app.Application
import android.service.notification.StatusBarNotification
import com.benasher44.uuid.Uuid
import dagger.BindsInstance
import dagger.Component
import io.rebble.cobble.NotificationChannelManager
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.DeviceTransport
import io.rebble.cobble.datasources.FlutterPreferences
import io.rebble.cobble.datasources.PairedStorage
import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.cobble.errors.GlobalExceptionHandler
import io.rebble.cobble.notifications.CallNotificationProcessor
import io.rebble.cobble.notifications.NotificationProcessor
import io.rebble.cobble.service.ServiceLifecycleControl
import io.rebble.cobble.shared.database.dao.NotificationChannelDao
import io.rebble.cobble.shared.datastore.KMPPrefs
import io.rebble.cobble.shared.domain.calendar.CalendarSync
import io.rebble.cobble.shared.jobs.AndroidJobScheduler
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.services.PhoneControlService
import io.rebble.libpebblecommon.services.ProtocolService
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import io.rebble.libpebblecommon.services.notification.NotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,
    LibPebbleModule::class
])
interface AppComponent {
    fun createNotificationService(): NotificationService
    fun createPhoneControlService(): PhoneControlService
    fun createBlueCommon(): DeviceTransport

    fun createProtocolHandler(): ProtocolHandler
    fun createBlobDBService(): BlobDBService

    fun createExceptionHandler(): GlobalExceptionHandler
    fun createConnectionLooper(): ConnectionLooper
    fun createWatchMetadataStore(): WatchMetadataStore
    fun createPairedStorage(): PairedStorage
    fun createNotificationProcessor(): NotificationProcessor
    fun createCallNotificationProcessor(): CallNotificationProcessor
    fun createFlutterPreferences(): FlutterPreferences
    fun initServiceLifecycleControl(): ServiceLifecycleControl
    fun initNotificationChannels(): NotificationChannelManager

    fun initLibPebbleCommonServices(): Set<ProtocolService>

    fun createActivitySubcomponentFactory(): ActivitySubcomponent.Factory
    fun createServiceSubcomponentFactory(): ServiceSubcomponent.Factory

    //TODO: Unify DI under Koin
    fun createKMPCalendarSync(): CalendarSync
    fun createKMPPrefs(): KMPPrefs
    fun createActiveNotifsState(): MutableStateFlow<Map<Uuid, StatusBarNotification>>
    fun createNotificationChannelDao(): NotificationChannelDao
    fun createAndroidJobScheduler(): AndroidJobScheduler

    @Component.Factory
    interface Factory {
        fun build(@BindsInstance application: Application): AppComponent
    }
}