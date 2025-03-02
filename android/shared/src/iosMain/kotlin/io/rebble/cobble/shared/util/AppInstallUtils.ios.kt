package io.rebble.cobble.shared.util

import io.rebble.cobble.shared.PlatformContext
import io.rebble.libpebblecommon.metadata.WatchType
import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import io.rebble.libpebblecommon.metadata.pbw.manifest.PbwManifest
import okio.Source

actual fun getPbwManifest(
    pbwFile: File,
    watchType: WatchType
): PbwManifest? {
    TODO("Not yet implemented")
}

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
actual fun requirePbwManifest(
    pbwFile: File,
    watchType: WatchType
): PbwManifest {
    TODO()
}

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
actual fun requirePbwAppInfo(pbwFile: File): PbwAppInfo {
    TODO()
}

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
actual fun requirePbwBinaryBlob(
    pbwFile: File,
    watchType: WatchType,
    blobName: String
): Source {
    TODO("Not yet implemented")
}

actual fun getPbwJsFilePath(
    context: PlatformContext,
    pbwAppInfo: PbwAppInfo,
    pbwFile: File
): String? {
    TODO()
}