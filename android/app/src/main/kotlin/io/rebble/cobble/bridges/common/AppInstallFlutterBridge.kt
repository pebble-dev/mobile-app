package io.rebble.cobble.bridges.common

import android.content.Context
import android.net.Uri
import com.squareup.moshi.Moshi
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.background.BackgroundAppInstallBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.data.pbw.PbwAppInfo
import io.rebble.cobble.data.pbw.toPigeon
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.util.launchPigeonResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import okio.source
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject

class AppInstallFlutterBridge @Inject constructor(
        private val context: Context,
        private val moshi: Moshi,
        private val coroutineScope: CoroutineScope,
        private val backgroundAppInstallBridge: BackgroundAppInstallBridge,
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
                var nextEntry: ZipEntry? = zipInputStream.nextEntry

                while (nextEntry != null) {
                    if (nextEntry.name == "appinfo.json") {
                        return@use parseAppInfoJson(zipInputStream)
                    }

                    zipInputStream.closeEntry()
                    nextEntry = zipInputStream.nextEntry
                }

                null
            }
        } catch (e: Exception) {
            Timber.e(e, "App parsing failed")
            null
        }
    }


    @Suppress("BlockingMethodInNonBlockingContext")
    override fun beginAppInstall(installData: Pigeons.InstallData) {
        coroutineScope.launch {
            // Copy pbw file to the app's folder
            val appsDir = File(context.filesDir, "apps")
            appsDir.mkdirs()
            val targetFileName = File(appsDir, "${installData.appInfo.uuid}.pbw")

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

    private fun parseAppInfoJson(stream: InputStream): PbwAppInfo? {
        return moshi.adapter(PbwAppInfo::class.java).fromJson(stream.source().buffer())
    }
}