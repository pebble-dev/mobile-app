package io.rebble.cobble.shared.util

import io.rebble.libpebblecommon.metadata.WatchType
import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import io.rebble.libpebblecommon.metadata.pbw.manifest.PbwManifest
import kotlinx.serialization.json.Json
import okio.Source
import okio.buffer

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}


expect fun getPbwManifest(pbwFile: File, watchType: WatchType): PbwManifest?

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
expect fun requirePbwManifest(pbwFile: File, watchType: WatchType): PbwManifest

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
expect fun requirePbwAppInfo(pbwFile: File): PbwAppInfo

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
expect fun requirePbwBinaryBlob(pbwFile: File, watchType: WatchType, blobName: String): Source
