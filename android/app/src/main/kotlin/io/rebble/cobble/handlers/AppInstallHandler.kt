package io.rebble.cobble.handlers

import android.content.Context
import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.cobble.middleware.PutBytesController
import io.rebble.cobble.middleware.getBestVariant
import io.rebble.cobble.shared.handlers.CobbleHandler
import io.rebble.cobble.util.getAppPbwFile
import io.rebble.cobble.util.requirePbwAppInfo
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import io.rebble.libpebblecommon.packets.AppFetchRequest
import io.rebble.libpebblecommon.packets.AppFetchResponse
import io.rebble.libpebblecommon.packets.AppFetchResponseStatus
import io.rebble.libpebblecommon.services.AppFetchService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject

class AppInstallHandler @Inject constructor(
        coroutineScope: CoroutineScope,
        private val context: Context,
        private val appFetchService: AppFetchService,
        private val putBytesController: PutBytesController,
        private val watchMetadataStore: WatchMetadataStore
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
        try {
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

            // Wait some time for metadata to become available in case this has been called
            // Right after watch has been connected
            val hardwarePlatformNumber = withTimeoutOrNull(2_000) {
                watchMetadataStore.lastConnectedWatchMetadata.first { it != null }
            }
                    ?.running
                    ?.hardwarePlatform
                    ?.get()

            if (hardwarePlatformNumber == null) {
                Timber.e("No watch metadata available. Cannot deduce watch type.")
                respondFetchRequest(AppFetchResponseStatus.NO_DATA)
                return
            }


            val connectedWatchType = WatchHardwarePlatform
                    .fromProtocolNumber(hardwarePlatformNumber)
                    ?.watchType

            if (connectedWatchType == null) {
                Timber.e("Unknown hardware platform %s", hardwarePlatformNumber)
                respondFetchRequest(AppFetchResponseStatus.NO_DATA)
                return
            }

            val appInfo = requirePbwAppInfo(appFile)

            val targetWatchType = getBestVariant(connectedWatchType, appInfo.targetPlatforms)
            if (targetWatchType == null) {
                Timber.e("Watch %s is not compatible with app %s Compatible apps: %s",
                        targetWatchType,
                        appUuid,
                        appInfo.targetPlatforms
                )
                respondFetchRequest(AppFetchResponseStatus.NO_DATA)
                return
            }


            respondFetchRequest(AppFetchResponseStatus.START)
            putBytesController.startAppInstall(message.appId.get(), appFile, targetWatchType)
        } catch (e: Exception) {
            Timber.e(e, "AppFetch fail")
            respondFetchRequest(AppFetchResponseStatus.NO_DATA)

        }
    }

    private suspend fun respondFetchRequest(status: AppFetchResponseStatus) {
        appFetchService.send(AppFetchResponse(status))
    }
}