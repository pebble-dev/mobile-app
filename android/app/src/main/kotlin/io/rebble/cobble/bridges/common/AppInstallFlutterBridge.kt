package io.rebble.cobble.bridges.common

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.data.pbw.appinfo.toPigeon
import io.rebble.cobble.shared.middleware.PutBytesController
import io.rebble.cobble.pigeons.NumberWrapper
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.shared.util.findFile
import io.rebble.cobble.util.launchPigeonResult
import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import io.rebble.libpebblecommon.packets.AppOrderResultCode
import io.rebble.libpebblecommon.packets.AppReorderRequest
import io.rebble.libpebblecommon.packets.blobdb.BlobResponse
import io.rebble.libpebblecommon.services.AppReorderService
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import timber.log.Timber
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipInputStream
import javax.inject.Inject

@Suppress("BlockingMethodInNonBlockingContext")
class AppInstallFlutterBridge @Inject constructor(
        private val context: Context,
        private val coroutineScope: CoroutineScope,
        private val reorderService: AppReorderService,
        private val putBytesController: PutBytesController,
        bridgeLifecycleController: BridgeLifecycleController
) : FlutterBridge, Pigeons.AppInstallControl {
    private val statusCallbacks = bridgeLifecycleController.createCallbacks(
            Pigeons::AppInstallStatusCallbacks
    )

    private var statusObservingJob: Job? = null

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }

    init {
        bridgeLifecycleController.setupControl(Pigeons.AppInstallControl::setup, this)
    }

    @Suppress("IfThenToElvis")
    override fun getAppInfo(
            arg: Pigeons.StringWrapper,
            result: Pigeons.Result<Pigeons.PbwAppInfo>) {
        coroutineScope.launchPigeonResult(result) {
            val uri: String = arg.value!!


            val parsingResult = parsePbwFileMetadata(uri)
            if (parsingResult != null) {
                parsingResult.toPigeon()
            } else {
                Pigeons.PbwAppInfo().apply { isValid = false; watchapp = Pigeons.WatchappInfo() }
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun parsePbwFileMetadata(
            uri: String
    ): PbwAppInfo? = withContext(Dispatchers.IO) {
        try {
            val stream = openUriStream(uri)

            ZipInputStream(stream).use { zipInputStream ->
                zipInputStream.findFile("appinfo.json") ?: return@use null
                parseAppInfoJson(zipInputStream)
            }
        } catch (e: Exception) {
            Timber.e(e, "App parsing failed")
            null
        }
    }

    override fun sendAppOrderToWatch(
            arg: Pigeons.ListWrapper,
            result: Pigeons.Result<Pigeons.NumberWrapper>
    ) {
        coroutineScope.launchPigeonResult(result) {
            val uuids = arg.value!!.map { UUID.fromString(it!! as String) }
            reorderService.send(
                    AppReorderRequest(uuids)
            )

            val resultPacket = withTimeoutOrNull(10_000) {
                reorderService.receivedMessages.receive()
            }

            if (resultPacket?.status?.get() == AppOrderResultCode.SUCCESS.value) {
                NumberWrapper(BlobResponse.BlobStatus.Success.value.toInt())
            } else {
                NumberWrapper(BlobResponse.BlobStatus.WatchDisconnected.value.toInt())
            }
        }

    }

    override fun subscribeToAppStatus() {
        statusObservingJob = coroutineScope.launch {
            putBytesController.status.collect {
                val statusPigeon = Pigeons.AppInstallStatus.Builder()
                        .setIsInstalling(it.state == PutBytesController.State.SENDING)
                        .setProgress(it.progress)
                        .build()

                statusCallbacks.onStatusUpdated(statusPigeon) {}
            }
        }
    }

    override fun unsubscribeFromAppStatus() {
        statusObservingJob?.cancel()
    }

    private fun openUriStream(uri: String): InputStream? {
        val parsedUri = Uri.parse(uri)

        return when (parsedUri.scheme) {
            "content" -> context.contentResolver.openInputStream(parsedUri)
            "file" -> parsedUri.toFile().inputStream().buffered()
            else -> {
                Timber.e("Unknown uri type: %s", uri)
                null
            }
        }
    }

    private fun parseAppInfoJson(stream: InputStream): PbwAppInfo? {
        return json.decodeFromStream(stream)
    }
}
