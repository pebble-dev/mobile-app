package io.rebble.cobble.shared.util

import io.rebble.cobble.shared.AndroidPlatformContext
import io.rebble.cobble.shared.PlatformContext
import io.rebble.libpebblecommon.metadata.WatchType
import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import io.rebble.libpebblecommon.metadata.pbw.manifest.PbwManifest
import kotlinx.serialization.json.decodeFromStream
import okio.Source
import okio.buffer

actual fun getPbwManifest(
    pbwFile: File,
    watchType: WatchType
): PbwManifest? {
    val manifestFile =
        pbwFile.zippedPlatformSource(watchType, "manifest.json")
            ?.buffer()
            ?: return null

    return manifestFile.use {
        json.decodeFromStream(it.inputStream())
    }
}

private fun File.zippedPlatformSource(
    watchType: WatchType,
    fileName: String
): Source? {
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
actual fun requirePbwManifest(
    pbwFile: File,
    watchType: WatchType
): PbwManifest {
    return getPbwManifest(pbwFile, watchType)
        ?: error("Manifest $watchType missing from app $pbwFile")
}

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
actual fun requirePbwAppInfo(pbwFile: File): PbwAppInfo {
    val appInfoFile =
        pbwFile.zippedSource("appinfo.json")
            ?.buffer()
            ?: error("appinfo.json missing from app $pbwFile")

    return appInfoFile.use {
        json.decodeFromStream(it.inputStream())
    }
}

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
actual fun requirePbwBinaryBlob(
    pbwFile: File,
    watchType: WatchType,
    blobName: String
): Source {
    return pbwFile.zippedPlatformSource(watchType, blobName)
        ?: error("Blob $blobName missing from app $pbwFile")
}

actual fun getPbwJsFilePath(
    context: PlatformContext,
    pbwAppInfo: PbwAppInfo,
    pbwFile: File
): String? {
    context as AndroidPlatformContext
    val cache = context.applicationContext.cacheDir.resolve("js")
    cache.mkdirs()
    val cachedJsFile = cache.resolve("${pbwAppInfo.uuid}-${pbwAppInfo.versionCode}.js")
    if (cachedJsFile.exists()) {
        return cachedJsFile.absolutePath
    }
    val jsFile =
        pbwFile.zippedSource("pebble-js-app.js")
            ?: return null

    cachedJsFile.bufferedWriter().use {
        it.write(jsFile.buffer().readUtf8())
    }

    return cachedJsFile.absolutePath
}