package io.rebble.cobble.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import io.rebble.cobble.BlueCommon
import io.rebble.cobble.errors.GlobalExceptionHandler
import io.rebble.libpebblecommon.ProtocolHandler
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

    @Component.Factory
    interface Factory {
        fun build(@BindsInstance application: Application): AppComponent
    }
}