package io.rebble.cobble.shared.handlers

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.read
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.api.RWS
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.middleware.PutBytesController
import io.rebble.cobble.shared.util.AppCompatibility.getBestVariant
import io.rebble.cobble.shared.util.File
import io.rebble.cobble.shared.util.requirePbwAppInfo
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import io.rebble.libpebblecommon.packets.AppFetchRequest
import io.rebble.libpebblecommon.packets.AppFetchResponse
import io.rebble.libpebblecommon.packets.AppFetchResponseStatus
import io.rebble.libpebblecommon.services.AppFetchService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppInstallHandler(
        pebbleDevice: PebbleDevice
): CobbleHandler, KoinComponent {
    private val lockerDao: LockerDao by inject()
    private val platformContext: PlatformContext by inject()
    private val appFetchService: AppFetchService by inject()
    private val httpClient: HttpClient by inject()
    private val putBytesController: PutBytesController by inject()

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

    private suspend fun downloadPbw(appUuid: String): String? {
        val row = lockerDao.getEntryByUuid(appUuid)
        val url = row?.entry?.pbwLink ?: run {
            Logging.e("App URL for $appUuid not found in locker")
            return null
        }

        val response = httpClient.get(url)
        if (response.status.value != 200) {
            Logging.e("Failed to download app $appUuid: ${response.status}")
            return null
        } else {
            return savePbwFile(platformContext, appUuid, response.bodyAsChannel())
        }
    }



    private suspend fun onNewAppFetchRequest(message: AppFetchRequest) {
        try {
            val appUuid = message.uuid.get().toString()
            var appFile = getAppPbwFile(platformContext, appUuid)

            if (!appFile.exists()) {
                val uri = downloadPbw(appUuid)
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
            val hardwarePlatformNumber = withTimeoutOrNull(2_000) {
                ConnectionStateManager.connectedWatchMetadata.first()
            }
                    ?.running
                    ?.hardwarePlatform
                    ?.get()

            if (hardwarePlatformNumber == null) {
                Logging.e("No watch metadata available. Cannot deduce watch type.")
                respondFetchRequest(AppFetchResponseStatus.NO_DATA)
                return
            }


            val connectedWatchType = WatchHardwarePlatform
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
                Logging.e("Watch $targetWatchType is not compatible with app $appUuid Compatible apps: ${appInfo.targetPlatforms}")
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
expect fun getAppPbwFile(context: PlatformContext, appUuid: String): File
expect suspend fun savePbwFile(context: PlatformContext, appUuid: String, byteReadChannel: ByteReadChannel): String