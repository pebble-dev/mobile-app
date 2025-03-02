package io.rebble.cobble.di

import android.app.Application
import android.service.notification.StatusBarNotification
import com.benasher44.uuid.Uuid
import dagger.BindsInstance
import dagger.Component
import io.rebble.cobble.NotificationChannelManager
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.DeviceTransport
import io.rebble.cobble.datasources.PairedStorage
import io.rebble.cobble.service.ServiceLifecycleControl
import io.rebble.cobble.shared.database.dao.NotificationChannelDao
import io.rebble.cobble.shared.datastore.KMPPrefs
import io.rebble.cobble.shared.domain.calendar.CalendarSync
import io.rebble.cobble.shared.errors.GlobalExceptionHandler
import io.rebble.cobble.shared.jobs.AndroidJobScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class
    ]
)
interface AppComponent {
    fun createBlueCommon(): DeviceTransport

    fun createExceptionHandler(): GlobalExceptionHandler

    fun createConnectionLooper(): ConnectionLooper

    fun createPairedStorage(): PairedStorage

    fun initServiceLifecycleControl(): ServiceLifecycleControl

    fun initNotificationChannels(): NotificationChannelManager

    fun createActivitySubcomponentFactory(): ActivitySubcomponent.Factory

    fun createFlutterActivitySubcomponentFactory(): FlutterActivitySubcomponent.Factory

    // TODO: Unify DI under Koin
    fun createKMPCalendarSync(): CalendarSync

    fun createKMPPrefs(): KMPPrefs

    fun createActiveNotifsState(): MutableStateFlow<Map<Uuid, StatusBarNotification>>

    fun createNotificationChannelDao(): NotificationChannelDao

    fun createAndroidJobScheduler(): AndroidJobScheduler

    @Component.Factory
    interface Factory {
        fun build(
            @BindsInstance application: Application
        ): AppComponent
    }
}