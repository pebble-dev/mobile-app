package io.rebble.cobble.util

import android.content.Context
import androidx.annotation.WorkerThread
import com.squareup.moshi.Moshi
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
    val manifestFileName = "${watchType.codename}/manifest.json"
    val manifestFile = pbwFile.zippedSource(manifestFileName)
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
fun requirePbwBinaryBlob(pbwFile: File, watchType: WatchType, blobName: String): Source {
    return pbwFile.zippedSource("${watchType.codename}/$blobName")
            ?: error("Blob ${blobName} missing from app $pbwFile")
}