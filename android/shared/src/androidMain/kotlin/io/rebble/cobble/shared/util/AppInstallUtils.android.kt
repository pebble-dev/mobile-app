package io.rebble.cobble.shared.util

import io.rebble.libpebblecommon.metadata.WatchType
import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import io.rebble.libpebblecommon.metadata.pbw.manifest.PbwManifest
import kotlinx.serialization.json.decodeFromStream
import okio.Source
import okio.buffer

actual fun getPbwManifest(
    pbwFile: File,
    watchType: WatchType
): PbwManifest?  {
    val manifestFile = pbwFile.zippedPlatformSource(watchType, "manifest.json")
            ?.buffer()
            ?: return null

    return manifestFile.use {
        json.decodeFromStream(it.inputStream())
    }
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

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
actual fun requirePbwManifest(pbwFile: File, watchType: WatchType): PbwManifest {
    return getPbwManifest(pbwFile, watchType)
            ?: error("Manifest $watchType missing from app $pbwFile")
}

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
actual fun requirePbwAppInfo(pbwFile: File): PbwAppInfo {
    val appInfoFile = pbwFile.zippedSource("appinfo.json")
            ?.buffer()
            ?: error("appinfo.json missing from app $pbwFile")

    return appInfoFile.use {
        json.decodeFromStream(it.inputStream())
    }
}

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
actual fun requirePbwBinaryBlob(pbwFile: File, watchType: WatchType, blobName: String): Source {
    return pbwFile.zippedPlatformSource(watchType, blobName)
            ?: error("Blob ${blobName} missing from app $pbwFile")
}