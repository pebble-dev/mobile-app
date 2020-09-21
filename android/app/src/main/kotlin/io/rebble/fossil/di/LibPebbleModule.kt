package io.rebble.fossil.di

import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.rebble.fossil.BlueCommon
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import io.rebble.libpebblecommon.services.notification.NotificationService
import javax.inject.Singleton

@Module
class LibPebbleModule {
    @Provides
    @Singleton
    fun provideProtocolHandler(
            blueCommon: BlueCommon
    ) = ProtocolHandler(blueCommon)

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