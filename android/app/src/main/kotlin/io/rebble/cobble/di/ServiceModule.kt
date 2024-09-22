package io.rebble.cobble.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.rebble.cobble.shared.handlers.music.MusicHandler
import io.rebble.cobble.shared.domain.notifications.NotificationActionHandler
import io.rebble.cobble.shared.handlers.AppInstallHandler
import io.rebble.cobble.shared.handlers.CalendarHandler
import io.rebble.cobble.shared.handlers.CobbleHandler
import kotlinx.coroutines.CoroutineScope
import javax.inject.Named

@Module
abstract class ServiceModule {
    @Module
    companion object {
        @Provides
        fun provideNotificationActionHandler(scope: CoroutineScope): NotificationActionHandler {
            return NotificationActionHandler(scope)
        }
    }

    //TODO: Move to per-protocol handler services
    /*
    @Binds
    @IntoSet
    @Named("normal")
    abstract fun bindAppMessageHandlerIntoSet(
            appMessageHandler: AppMessageHandler
    ): CobbleHandler
    */

    @Binds
    @IntoSet
    @Named("normal")
    abstract fun bindCalendarHandlerIntoSet(
            calendarHandler: CalendarHandler
    ): CobbleHandler
    @Binds
    @IntoSet
    @Named("normal")
    abstract fun bindNotificationActionHandlerIntoSet(
            notificationHandler: NotificationActionHandler
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
    abstract fun bindAppInstallHandlerIntoSet(
            appInstallHandler: AppInstallHandler
    ): CobbleHandler
}