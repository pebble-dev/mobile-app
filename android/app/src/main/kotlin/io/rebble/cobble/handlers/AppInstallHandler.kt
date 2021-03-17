package io.rebble.cobble.handlers

import android.content.Context
import io.rebble.cobble.di.PerService
import io.rebble.cobble.middleware.PutBytesController
import io.rebble.cobble.util.getAppPbwFile
import io.rebble.libpebblecommon.metadata.WatchType
import io.rebble.libpebblecommon.packets.AppFetchRequest
import io.rebble.libpebblecommon.packets.AppFetchResponse
import io.rebble.libpebblecommon.packets.AppFetchResponseStatus
import io.rebble.libpebblecommon.services.AppFetchService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@PerService
class AppInstallHandler @Inject constructor(
        coroutineScope: CoroutineScope,
        private val context: Context,
        private val appFetchService: AppFetchService,
        private val putBytesController: PutBytesController
) : CobbleHandler {
    init {
        coroutineScope.launch {
            for (message in appFetchService.receivedMessages) {
                when (message) {
                    is AppFetchRequest -> onNewAppFetchRequest(message)
                }
            }
        }
    }

    private suspend fun onNewAppFetchRequest(message: AppFetchRequest) {
        val appUuid = message.uuid.get().toString()
        val appFile = getAppPbwFile(context, appUuid)

        if (!appFile.exists()) {
            respondFetchRequest(AppFetchResponseStatus.INVALID_UUID)
            Timber.e("Watch requested nonexistent app data (%s)", appUuid)
            return
        }

        if (putBytesController.status.value.state != PutBytesController.State.IDLE) {
            Timber.e("Watch requested new app data PutBytes is busy")
            respondFetchRequest(AppFetchResponseStatus.BUSY)
            return
        }

        // TODO deduce this from connected watch
        val watchType = WatchType.BASALT

        respondFetchRequest(AppFetchResponseStatus.START)
        putBytesController.startAppInstall(message.appId.get(), appFile, watchType)
    }

    private suspend fun respondFetchRequest(status: AppFetchResponseStatus) {
        appFetchService.send(AppFetchResponse(status))
    }
}