package io.rebble.cobble.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import io.rebble.cobble.NotificationChannelManager
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.DeviceTransport
import io.rebble.cobble.datasources.FlutterPreferences
import io.rebble.cobble.datasources.PairedStorage
import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.cobble.errors.GlobalExceptionHandler
import io.rebble.cobble.notifications.NotificationProcessor
import io.rebble.cobble.service.ServiceLifecycleControl
import io.rebble.cobble.shared.datastore.KMPPrefs
import io.rebble.cobble.shared.domain.calendar.CalendarSync
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.services.PhoneControlService
import io.rebble.libpebblecommon.services.ProtocolService
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import io.rebble.libpebblecommon.services.notification.NotificationService
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
    fun createFlutterPreferences(): FlutterPreferences
    fun initServiceLifecycleControl(): ServiceLifecycleControl
    fun initNotificationChannels(): NotificationChannelManager

    fun initLibPebbleCommonServices(): Set<ProtocolService>

    fun createActivitySubcomponentFactory(): ActivitySubcomponent.Factory
    fun createServiceSubcomponentFactory(): ServiceSubcomponent.Factory

    //TODO: Unify DI under Koin
    fun createKMPCalendarSync(): CalendarSync
    fun createKMPPrefs(): KMPPrefs

    @Component.Factory
    interface Factory {
        fun build(@BindsInstance application: Application): AppComponent
    }
}