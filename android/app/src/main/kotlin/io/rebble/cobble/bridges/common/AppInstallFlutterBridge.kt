package io.rebble.cobble.bridges.common

import android.content.Context
import android.net.Uri
import com.squareup.moshi.Moshi
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.background.BackgroundAppInstallBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.data.pbw.appinfo.PbwAppInfo
import io.rebble.cobble.data.pbw.appinfo.toPigeon
import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.cobble.pigeons.NumberWrapper
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.util.*
import io.rebble.libpebblecommon.disk.PbwBinHeader
import io.rebble.libpebblecommon.metadata.WatchType
import io.rebble.libpebblecommon.packets.blobdb.BlobCommand
import io.rebble.libpebblecommon.packets.blobdb.BlobResponse
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okio.buffer
import okio.sink
import okio.source
import timber.log.Timber
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject
import kotlin.random.Random

@Suppress("BlockingMethodInNonBlockingContext")
class AppInstallFlutterBridge @Inject constructor(
        private val context: Context,
        private val moshi: Moshi,
        private val coroutineScope: CoroutineScope,
        private val backgroundAppInstallBridge: BackgroundAppInstallBridge,
        private val watchMetadataStore: WatchMetadataStore,
        private val blobDBService: BlobDBService,
        bridgeLifecycleController: BridgeLifecycleController
) : FlutterBridge, Pigeons.AppInstallControl {
    init {
        bridgeLifecycleController.setupControl(Pigeons.AppInstallControl::setup, this)
    }

    @Suppress("IfThenToElvis")
    override fun getAppInfo(
            arg: Pigeons.StringWrapper,
            result: Pigeons.Result<Pigeons.PbwAppInfo>) {
        coroutineScope.launchPigeonResult(result) {
            val uri: String = arg.value


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
            val stream = context.contentResolver.openInputStream(Uri.parse(uri))

            ZipInputStream(stream).use { zipInputStream ->
                zipInputStream.findFile("appinfo.json") ?: return@use null
                parseAppInfoJson(zipInputStream)
            }
        } catch (e: Exception) {
            Timber.e(e, "App parsing failed")
            null
        }
    }


    override fun beginAppInstall(installData: Pigeons.InstallData) {
        coroutineScope.launch {
            // Copy pbw file to the app's folder
            val appUuid = installData.appInfo.uuid
            val targetFileName = getAppPbwFile(context, appUuid)

            val success = withContext(Dispatchers.IO) {
                val openInputStream = context.contentResolver
                        .openInputStream(Uri.parse(installData.uri))

                if (openInputStream == null) {
                    Timber.e("Unknown URI '%s'. This should have been filtered before it reached beginAppInstall. Aborting.", installData.uri)
                    return@withContext false
                }

                val source = openInputStream
                        .source()
                        .buffer()

                val sink = targetFileName.sink().buffer()

                source.use {
                    sink.use {
                        sink.writeAll(source)
                    }
                }

                true
            }

            if (success) {
                backgroundAppInstallBridge.installAppNow(installData.uri, installData.appInfo)
            }
        }
    }

    override fun insertAppIntoBlobDb(arg: Pigeons.StringWrapper, result: Pigeons.Result<Pigeons.NumberWrapper>) {
        coroutineScope.launchPigeonResult(result, Dispatchers.IO) {
            NumberWrapper(try {
                val appUuid = arg.value

                val appFile = getAppPbwFile(context, appUuid)
                if (!appFile.exists()) {
                    error("PBW file $appUuid missing")
                }


                // Wait some time for metadata to become available in case this has been called
                // Right after watch has been connected

                val hardwarePlatformNumber = withTimeoutOrNull(2_000) {
                    watchMetadataStore.lastConnectedWatchMetadata.first { it != null }
                }
                        ?.running
                        ?.hardwarePlatform
                        ?.get()
                        ?: error("Watch not connected")

                // TODO Always install basalt version for now,
                // until compatibility logic is ready
                val watchType = WatchType.BASALT
                /*WatchHardwarePlatform.fromProtocolNumber(hardwarePlatformNumber)
                        ?.watchType
                        ?: error("Unknown hardware platform $hardwarePlatformNumber")*/


                val manifest = requirePbwManifest(moshi, appFile, watchType)

                Timber.d("Manifest %s", manifest)

                val appBlob = requirePbwBinaryBlob(appFile, watchType, manifest.application.name)

                val headerData = appBlob
                        .buffer().use { it.readByteArray(PbwBinHeader.SIZE.toLong()) }

                val parsedHeader = PbwBinHeader.parseFileHeader(headerData.toUByteArray())

                val insertResult = blobDBService.send(
                        BlobCommand.InsertCommand(
                                Random.nextInt(0, UShort.MAX_VALUE.toInt()).toUShort(),
                                BlobCommand.BlobDatabase.App,
                                parsedHeader.uuid.toBytes(),
                                parsedHeader.toBlobDbApp().toBytes()
                        )
                )

                insertResult.responseValue.value.toInt()
            } catch (e: Exception) {
                Timber.e(e, "Failed to send PBW file to the BlobDB")
                BlobResponse.BlobStatus.GeneralFailure.value.toInt()
            })
        }

    }

    private fun parseAppInfoJson(stream: InputStream): PbwAppInfo? {
        return moshi.adapter(PbwAppInfo::class.java).fromJson(stream.source().buffer())
    }
}