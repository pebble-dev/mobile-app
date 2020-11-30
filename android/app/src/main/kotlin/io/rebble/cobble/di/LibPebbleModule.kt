package io.rebble.cobble.di

import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.rebble.cobble.BlueCommon
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.ProtocolHandlerImpl
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import io.rebble.libpebblecommon.services.notification.NotificationService
import javax.inject.Singleton

@Module
class LibPebbleModule {
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
    @Reusable
    fun provideNotificationService(
            blobDBService: BlobDBService
    ) = NotificationService(blobDBService)
}