package io.rebble.cobble.di

import dagger.BindsInstance
import dagger.Subcomponent
import io.rebble.cobble.shared.handlers.CobbleHandler
import io.rebble.cobble.service.WatchService
import javax.inject.Named
import javax.inject.Provider

@Subcomponent(
        modules = [
            ServiceModule::class
        ]
)
interface ServiceSubcomponent {
    @Named("negotiation")
    fun getNegotiationMessageHandlersProvider(): Provider<Set<CobbleHandler>>

    @Named("normal")
    fun getNormalMessageHandlersProvider(): Provider<Set<CobbleHandler>>

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance watchService: WatchService): ServiceSubcomponent
    }
}