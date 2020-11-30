package io.rebble.cobble.di

import dagger.BindsInstance
import dagger.Subcomponent
import io.rebble.cobble.handlers.PebbleMessageHandler
import io.rebble.cobble.service.WatchService
import javax.inject.Scope

@PerService
@Subcomponent(
        modules = [
            ServiceModule::class
        ]
)
interface ServiceSubcomponent {
    fun initAllMessageHandlers(): Set<PebbleMessageHandler>

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance watchService: WatchService): ServiceSubcomponent
    }
}

@Scope
annotation class PerService