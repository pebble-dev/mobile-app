package io.rebble.cobble.shared.handlers

import io.ktor.client.HttpClient
import io.ktor.utils.io.ByteReadChannel
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.middleware.PutBytesController
import io.rebble.cobble.shared.util.AppCompatibility.getBestVariant
import io.rebble.cobble.shared.util.File
import io.rebble.cobble.shared.util.requirePbwAppInfo
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import io.rebble.libpebblecommon.packets.AppFetchRequest
import io.rebble.libpebblecommon.packets.AppFetchResponse
import io.rebble.libpebblecommon.packets.AppFetchResponseStatus
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppInstallHandler(
    private val pebbleDevice: PebbleDevice
) : CobbleHandler, KoinComponent {
    private val lockerDao: LockerDao by inject()
    private val platformContext: PlatformContext by inject()
    private val httpClient: HttpClient by inject()
    private val putBytesController = pebbleDevice.putBytesController
    private val appFetchService = pebbleDevice.appFetchService

    init {
        pebbleDevice.negotiationScope.launch {
            val deviceScope = pebbleDevice.connectionScope.filterNotNull().first()
            deviceScope.launch {
                for (message in appFetchService.receivedMessages) {
                    when (message) {
                        is AppFetchRequest -> onNewAppFetchRequest(message)
                    }
                }
            }
        }
    }

    private suspend fun onNewAppFetchRequest(message: AppFetchRequest) {
        try {
            val appUuid = message.uuid.get().toString()
            var appFile = getAppPbwFile(platformContext, appUuid)

            if (!appFile.exists()) {
                val uri = downloadPbw(platformContext, httpClient, lockerDao, appUuid)
                if (uri == null) {
                    Logging.e("Failed to download app $appUuid")
                    respondFetchRequest(AppFetchResponseStatus.NO_DATA)
                    return
                }
                appFile = File(uri)
                if (!appFile.exists()) {
                    Logging.e("Downloaded app file does not exist")
                    respondFetchRequest(AppFetchResponseStatus.NO_DATA)
                    return
                }
            }

            if (putBytesController.status.value.state != PutBytesController.State.IDLE) {
                Logging.e("Watch requested new app data PutBytes is busy")
                respondFetchRequest(AppFetchResponseStatus.BUSY)
                return
            }

            // Wait some time for metadata to become available in case this has been called
            // Right after watch has been connected
            val hardwarePlatformNumber =
                withTimeoutOrNull(2_000) {
                    pebbleDevice.metadata.first()
                }
                    ?.running
                    ?.hardwarePlatform
                    ?.get()

            if (hardwarePlatformNumber == null) {
                Logging.e("No watch metadata available. Cannot deduce watch type.")
                respondFetchRequest(AppFetchResponseStatus.NO_DATA)
                return
            }

            val connectedWatchType =
                WatchHardwarePlatform
                    .fromProtocolNumber(hardwarePlatformNumber)
                    ?.watchType

            if (connectedWatchType == null) {
                Logging.e("Unknown hardware platform $hardwarePlatformNumber")
                respondFetchRequest(AppFetchResponseStatus.NO_DATA)
                return
            }

            val appInfo = requirePbwAppInfo(appFile)

            val targetWatchType = getBestVariant(connectedWatchType, appInfo.targetPlatforms)
            if (targetWatchType == null) {
                Logging.e(
                    "Watch $targetWatchType is not compatible with app $appUuid Compatible apps: ${appInfo.targetPlatforms}"
                )
                respondFetchRequest(AppFetchResponseStatus.NO_DATA)
                return
            }

            respondFetchRequest(AppFetchResponseStatus.START)
            putBytesController.startAppInstall(message.appId.get(), appFile, targetWatchType)
        } catch (e: Exception) {
            Logging.e("AppFetch fail", e)
            respondFetchRequest(AppFetchResponseStatus.NO_DATA)
        }
    }

    private suspend fun respondFetchRequest(status: AppFetchResponseStatus) {
        appFetchService.send(AppFetchResponse(status))
    }
}

expect fun getAppPbwFile(
    context: PlatformContext,
    appUuid: String
): File

expect suspend fun savePbwFile(
    context: PlatformContext,
    appUuid: String,
    byteReadChannel: ByteReadChannel
): String

expect suspend fun downloadPbw(
    context: PlatformContext,
    httpClient: HttpClient,
    lockerDao: LockerDao,
    appUuid: String
): String?