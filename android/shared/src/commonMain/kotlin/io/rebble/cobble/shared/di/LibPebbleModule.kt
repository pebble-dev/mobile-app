package io.rebble.cobble.shared.di

import io.rebble.cobble.shared.middleware.PutBytesController
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.ProtocolHandlerImpl
import io.rebble.libpebblecommon.services.*
import io.rebble.libpebblecommon.services.app.AppRunStateService
import io.rebble.libpebblecommon.services.appmessage.AppMessageService
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import io.rebble.libpebblecommon.services.blobdb.TimelineService
import org.koin.dsl.bind
import org.koin.dsl.module

val libpebbleModule =
    module {
        factory {
            ProtocolHandlerImpl()
        } bind ProtocolHandler::class

        factory { params ->
            TimelineService(params.get())
        }
        factory { params ->
            PutBytesService(params.get())
        }
        factory { params ->
            AppFetchService(params.get())
        }
        factory { params ->
            PutBytesController(params.get())
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

        factory { params ->
            PhoneControlService(params.get())
        }

        factory { params ->
            AppLogService(params.get())
        }

        factory { params ->
            LogDumpService(params.get())
        }

        factory { params ->
            ScreenshotService(params.get())
        }

        factory { params ->
            VoiceService(params.get())
        }

        factory { params ->
            AudioStreamService(params.get())
        }
    }