package io.rebble.fossil.di

import dagger.BindsInstance
import dagger.Subcomponent
import io.rebble.fossil.handlers.PebbleMessageHandler
import io.rebble.fossil.service.WatchService
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