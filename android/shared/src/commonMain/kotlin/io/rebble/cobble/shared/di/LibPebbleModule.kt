package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.middleware.PutBytesController
import io.rebble.libpebblecommon.ProtocolHandlerImpl
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.services.AppFetchService
import io.rebble.libpebblecommon.services.MusicService
import io.rebble.libpebblecommon.services.PutBytesService
import io.rebble.libpebblecommon.services.SystemService
import io.rebble.libpebblecommon.services.app.AppRunStateService
import io.rebble.libpebblecommon.services.appmessage.AppMessageService
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import org.koin.dsl.module
import io.rebble.libpebblecommon.services.blobdb.TimelineService

val libpebbleModule = module {
    //TODO: Move away from global protocol handler and singleton services
    single<ProtocolHandler> {
        ProtocolHandlerImpl()
    }
    single {
        TimelineService(get())
    }
    single {
        PutBytesService(get())
    }
    single {
        AppFetchService(get())
    }
    single {
        PutBytesController()
    }

    factory { params ->
        AppRunStateService(params.get())
    }

    factory { params ->
        BlobDBService(params.get())
    }

    factory { params ->
        AppMessageService(params.get())
    }

    factory { params ->
        MusicService(params.get())
    }

    factory { params ->
        SystemService(params.get())
    }
}