package io.rebble.cobble.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.rebble.cobble.handlers.*
import io.rebble.cobble.handlers.music.MusicHandler
import io.rebble.cobble.service.WatchService
import kotlinx.coroutines.CoroutineScope
import javax.inject.Named

@Module
abstract class ServiceModule {
    @Module
    companion object {
        @Provides
        fun provideCoroutineScope(watchService: WatchService): CoroutineScope {
            return watchService.watchConnectionScope
        }
    }

    @Binds
    @IntoSet
    @Named("normal")
    abstract fun bindAppMessageHandlerIntoSet(
            appMessageHandler: AppMessageHandler
    ): CobbleHandler

    @Binds
    @IntoSet
    @Named("negotiation")
    abstract fun bindSystemMessageHandlerIntoSet(
            systemMessageHandler: SystemHandler
    ): CobbleHandler

    @Binds
    @IntoSet
    @Named("normal")
    abstract fun bindCalendarHandlerIntoSet(
            calendarHandler: CalendarHandler
    ): CobbleHandler

    @Binds
    @IntoSet
    @Named("normal")
    abstract fun bindTimelineHandlerIntoSet(
            timelineHandler: TimelineHandler
    ): CobbleHandler

    @Binds
    @IntoSet
    @Named("normal")
    abstract fun bindMusicHandlerIntoSet(
            musicHandler: MusicHandler
    ): CobbleHandler

    @Binds
    @IntoSet
    @Named("normal")
    abstract fun bindFlutterBackgroundStart(
            flutterStartHandler: FlutterStartHandler
    ): CobbleHandler

    @Binds
    @IntoSet
    @Named("normal")
    abstract fun bindAppInstallHandlerIntoSet(
            appInstallHandler: AppInstallHandler
    ): CobbleHandler

    @Binds
    @IntoSet
    @Named("normal")
    abstract fun bindAppRunStateHandler(
            appRunStateHandler: AppRunStateHandler
    ): CobbleHandler
}