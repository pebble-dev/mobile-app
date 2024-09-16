package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.middleware.PutBytesController
import io.rebble.libpebblecommon.ProtocolHandlerImpl
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.services.AppFetchService
import io.rebble.libpebblecommon.services.PutBytesService
import org.koin.dsl.module
import io.rebble.libpebblecommon.services.blobdb.TimelineService

val libpebbleModule = module {
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
}