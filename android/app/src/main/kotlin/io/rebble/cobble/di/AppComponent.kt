package io.rebble.cobble.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import io.rebble.cobble.NotificationChannelManager
import io.rebble.cobble.transport.bluetooth.BlueCommon
import io.rebble.cobble.transport.bluetooth.ConnectionLooper
import io.rebble.cobble.bridges.background.BackgroundTimelineFlutterBridge
import io.rebble.cobble.bridges.background.CalendarFlutterBridge
import io.rebble.cobble.datasources.FlutterPreferences
import io.rebble.cobble.datasources.PairedStorage
import io.rebble.cobble.errors.GlobalExceptionHandler
import io.rebble.cobble.service.ServiceLifecycleControl
import io.rebble.cobble.transport.emulator.EmuSocketDriver
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
    fun createEmuDriver(): EmuSocketDriver
    fun createProtocolHandler(): ProtocolHandler
    fun createExceptionHandler(): GlobalExceptionHandler
    fun createConnectionLooper(): ConnectionLooper
    fun createPairedStorage(): PairedStorage
    fun createCalendarFlutterBridge(): CalendarFlutterBridge
    fun createTimelineSyncFlutterBridge(): BackgroundTimelineFlutterBridge
    fun createFlutterPreferences(): FlutterPreferences
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