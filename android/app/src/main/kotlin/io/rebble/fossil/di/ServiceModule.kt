package io.rebble.fossil.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.rebble.fossil.handlers.AppMessageHandler
import io.rebble.fossil.handlers.PebbleMessageHandler
import io.rebble.fossil.handlers.SystemHandler
import io.rebble.fossil.service.WatchService
import kotlinx.coroutines.CoroutineScope

@Module
abstract class ServiceModule {
    @Module
    companion object {
        @Provides
        fun provideCoroutineScope(watchService: WatchService): CoroutineScope {
            return watchService.coroutineScope
        }
    }

    @Binds
    @IntoSet
    abstract fun bindAppMessageHandlerIntoSet(
            appMessageHandler: AppMessageHandler
    ): PebbleMessageHandler

    @Binds
    @IntoSet
    abstract fun bindSystemMessageHandlerIntoSet(
            systemMessageHandler: SystemHandler
    ): PebbleMessageHandler
}