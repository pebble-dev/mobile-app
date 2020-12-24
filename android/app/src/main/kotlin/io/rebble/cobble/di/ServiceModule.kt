package io.rebble.cobble.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.rebble.cobble.handlers.*
import io.rebble.cobble.handlers.music.MusicHandler
import io.rebble.cobble.service.WatchService
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

    @Binds
    @IntoSet
    abstract fun bindCalendarHandlerIntoSet(
            calendarHandler: CalendarHandler
    ): PebbleMessageHandler

    @Binds
    @IntoSet
    abstract fun bindTimelineHandlerIntoSet(
            timelineHandler: TimelineHandler
    ): PebbleMessageHandler

    @Binds
    @IntoSet
    abstract fun bindMusicHandlerIntoSet(
            musicHandler: MusicHandler
    ): PebbleMessageHandler
}