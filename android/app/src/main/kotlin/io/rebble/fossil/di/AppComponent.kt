package io.rebble.fossil.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import io.rebble.fossil.NotificationChannelManager
import io.rebble.fossil.bluetooth.BlueCommon
import io.rebble.fossil.bluetooth.ConnectionLooper
import io.rebble.fossil.datasources.PairedStorage
import io.rebble.fossil.errors.GlobalExceptionHandler
import io.rebble.fossil.service.ServiceLifecycleControl
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.services.ProtocolService
import io.rebble.libpebblecommon.services.notification.NotificationService
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,
    LibPebbleModule::class
])
interface AppComponent {
    fun createNotificationService(): NotificationService
    fun createBlueCommon(): BlueCommon
    fun createProtocolHandler(): ProtocolHandler
    fun createExceptionHandler(): GlobalExceptionHandler
    fun createConnectionLooper(): ConnectionLooper
    fun createPairedStorage(): PairedStorage
    fun initServiceLifecycleControl(): ServiceLifecycleControl
    fun initNotificationChannels(): NotificationChannelManager

    fun initLibPebbleCommonServices(): Set<ProtocolService>

    fun createActivitySubcomponentFactory(): ActivitySubcomponent.Factory
    fun createServiceSubcomponentFactory(): ServiceSubcomponent.Factory

    @Component.Factory
    interface Factory {
        fun build(@BindsInstance application: Application): AppComponent
    }
}