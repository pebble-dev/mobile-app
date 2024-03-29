package io.rebble.cobble.di

import dagger.BindsInstance
import dagger.Subcomponent
import io.rebble.cobble.handlers.CobbleHandler
import io.rebble.cobble.service.WatchService
import javax.inject.Provider

@Subcomponent(
        modules = [
            ServiceModule::class
        ]
)
interface ServiceSubcomponent {
    fun getMessageHandlersProvider(): Provider<Set<CobbleHandler>>

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance watchService: WatchService): ServiceSubcomponent
    }
}