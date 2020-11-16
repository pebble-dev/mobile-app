package io.rebble.fossil.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import io.rebble.fossil.bluetooth.BlueCommon
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.ProtocolHandlerImpl
import io.rebble.libpebblecommon.services.ProtocolService
import io.rebble.libpebblecommon.services.SystemService
import io.rebble.libpebblecommon.services.app.AppRunStateService
import io.rebble.libpebblecommon.services.appmessage.AppMessageService
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import io.rebble.libpebblecommon.services.notification.NotificationService
import javax.inject.Singleton

@Module
abstract class LibPebbleModule {
    @Module
    companion object {
        @Provides
        @Singleton
        fun provideProtocolHandler(
                blueCommon: BlueCommon
        ): ProtocolHandler = ProtocolHandlerImpl(blueCommon)

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
}