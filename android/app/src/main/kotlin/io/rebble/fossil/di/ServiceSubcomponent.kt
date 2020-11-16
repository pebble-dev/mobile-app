package io.rebble.fossil.di

import dagger.BindsInstance
import dagger.Subcomponent
import io.rebble.fossil.handlers.AppMessageHandler
import io.rebble.fossil.handlers.SystemHandler
import io.rebble.fossil.service.WatchService
import javax.inject.Scope

@PerService
@Subcomponent(
        modules = [
            ServiceModule::class
        ]
)
interface ServiceSubcomponent {
    fun createAppMessageHandler(): AppMessageHandler
    fun createSystemHandler(): SystemHandler

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance watchService: WatchService): ServiceSubcomponent
    }
}

@Scope
annotation class PerService