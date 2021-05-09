package io.rebble.cobble.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.ProtocolHandlerImpl
import io.rebble.libpebblecommon.services.*
import io.rebble.libpebblecommon.services.app.AppRunStateService
import io.rebble.libpebblecommon.services.appmessage.AppMessageService
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import io.rebble.libpebblecommon.services.blobdb.TimelineService
import io.rebble.libpebblecommon.services.notification.NotificationService
import javax.inject.Singleton

@Module
abstract class LibPebbleModule {
    @Module
    companion object {
        @Provides
        @Singleton
        fun provideProtocolHandler(): ProtocolHandler = ProtocolHandlerImpl()

        @Provides
        @Singleton
        fun provideBlobDbService(
                protocolHandler: ProtocolHandler
        ) = BlobDBService(protocolHandler)

        @Provides
        @Singleton
        fun provideNotificationService(
                blobDBService: BlobDBService
        ) = NotificationService(blobDBService)

        @Provides
        @Singleton
        fun provideAppMessageService(
                protocolHandler: ProtocolHandler
        ) = AppMessageService(protocolHandler)

        @Provides
        @Singleton
        fun provideAppRunStateService(
                protocolHandler: ProtocolHandler
        ) = AppRunStateService(protocolHandler)

        @Provides
        @Singleton
        fun provideSystemService(
                protocolHandler: ProtocolHandler
        ) = SystemService(protocolHandler)

        @Provides
        @Singleton
        fun provideTimelineService(
                protocolHandler: ProtocolHandler
        ) = TimelineService(protocolHandler)

        @Provides
        @Singleton
        fun provideMusicService(
                protocolHandler: ProtocolHandler
        ) = MusicService(protocolHandler)

        @Provides
        @Singleton
        fun provideAppFetchService(
                protocolHandler: ProtocolHandler
        ) = AppFetchService(protocolHandler)

        @Provides
        @Singleton
        fun providePutBytesService(
                protocolHandler: ProtocolHandler
        ) = PutBytesService(protocolHandler)

        @Provides
        @Singleton
        fun provideAppReorderService(
                protocolHandler: ProtocolHandler
        ) = AppReorderService(protocolHandler)

        @Provides
        @Singleton
        fun provideScreenshotService(
                protocolHandler: ProtocolHandler
        ) = ScreenshotService(protocolHandler)
    }

    @Binds
    @IntoSet
    abstract fun bindBlobDbServiceIntoSet(blobDBService: BlobDBService): ProtocolService

    @Binds
    @IntoSet
    abstract fun bindNotificationService(notificationService: NotificationService): ProtocolService

    @Binds
    @IntoSet
    abstract fun bindAppMessageServiceIntoSet(appMessageService: AppMessageService): ProtocolService

    @Binds
    @IntoSet
    abstract fun bindAppRunStateServiceIntoSet(
            appRunStateService: AppRunStateService
    ): ProtocolService

    @Binds
    @IntoSet
    abstract fun bindSystemServiceIntoSet(systemService: SystemService): ProtocolService

    @Binds
    @IntoSet
    abstract fun bindTimelineServiceIntoSet(timelineService: TimelineService): ProtocolService

    @Binds
    @IntoSet
    abstract fun bindMusicServiceIntoSet(musicService: MusicService): ProtocolService

    @Binds
    @IntoSet
    abstract fun bindAppFetchServiceIntoSet(service: AppFetchService): ProtocolService

    @Binds
    @IntoSet
    abstract fun bindPutBytesServiceIntoSet(service: PutBytesService): ProtocolService

    @Binds
    @IntoSet
    abstract fun bindAppReorderServiceIntoSet(service: AppReorderService): ProtocolService

    @Binds
    @IntoSet
    abstract fun bindScrenshotServiceIntoSet(service: ScreenshotService): ProtocolService
}