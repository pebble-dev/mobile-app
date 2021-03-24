package io.rebble.cobble.util

import android.content.Context
import androidx.annotation.WorkerThread
import com.squareup.moshi.Moshi
import io.rebble.cobble.data.pbw.appinfo.PbwAppInfo
import io.rebble.cobble.data.pbw.manifest.PbwManifest
import io.rebble.libpebblecommon.metadata.WatchType
import okio.Source
import okio.buffer
import java.io.File

fun getAppPbwFile(context: Context, appUuid: String): File {
    val appsDir = File(context.filesDir, "apps")
    appsDir.mkdirs()
    val targetFileName = File(appsDir, "$appUuid.pbw")
    return targetFileName
}

@WorkerThread
fun getPbwManifest(moshi: Moshi, pbwFile: File, watchType: WatchType): PbwManifest? {
    val manifestFile = pbwFile.zippedPlatformSource(watchType, "manifest.json")
            ?.buffer()
            ?: return null

    return manifestFile.use {
        moshi.adapter(PbwManifest::class.java).nonNull().fromJson(it)!!
    }
}

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
fun requirePbwManifest(moshi: Moshi, pbwFile: File, watchType: WatchType): PbwManifest {
    return getPbwManifest(moshi, pbwFile, watchType)
            ?: error("Manifest $watchType missing from app $pbwFile")
}

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
fun requirePbwAppInfo(moshi: Moshi, pbwFile: File): PbwAppInfo {
    val appInfoFile = pbwFile.zippedSource("appinfo.json")
            ?.buffer()
            ?: error("appinfo.json missing from app $pbwFile")

    return appInfoFile.use {
        moshi.adapter(PbwAppInfo::class.java).nonNull().fromJson(it)!!
    }
}

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
fun requirePbwBinaryBlob(pbwFile: File, watchType: WatchType, blobName: String): Source {
    return pbwFile.zippedPlatformSource(watchType, blobName)
            ?: error("Blob ${blobName} missing from app $pbwFile")
}

private fun File.zippedPlatformSource(watchType: WatchType, fileName: String): Source? {
    return if (watchType == WatchType.APLITE) {
        // Older aplite-only releases do not have folders for different platforms
        // Everything is in root
        zippedSource("aplite/$fileName") ?: zippedSource(fileName)
    } else {
        zippedSource("${watchType.codename}/$fileName")
    }
}