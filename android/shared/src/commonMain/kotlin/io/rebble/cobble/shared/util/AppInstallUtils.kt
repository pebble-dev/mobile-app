package io.rebble.cobble.shared.util

import io.rebble.cobble.shared.PlatformContext
import io.rebble.libpebblecommon.metadata.WatchType
import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import io.rebble.libpebblecommon.metadata.pbw.manifest.PbwManifest
import kotlinx.serialization.json.Json
import okio.Source

val json =
    Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

expect fun getPbwManifest(
    pbwFile: File,
    watchType: WatchType
): PbwManifest?

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
expect fun requirePbwManifest(
    pbwFile: File,
    watchType: WatchType
): PbwManifest

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
expect fun requirePbwAppInfo(pbwFile: File): PbwAppInfo

/**
 * @throws IllegalStateException if pbw does not contain manifest with that watch type
 */
expect fun requirePbwBinaryBlob(
    pbwFile: File,
    watchType: WatchType,
    blobName: String
): Source

expect fun getPbwJsFilePath(
    context: PlatformContext,
    pbwAppInfo: PbwAppInfo,
    pbwFile: File
): String?

/**
 * @throws IllegalStateException if pbw does not contain js file
 */
fun requirePbwJsFilePath(
    context: PlatformContext,
    pbwAppInfo: PbwAppInfo,
    pbwFile: File
): String {
    return getPbwJsFilePath(context, pbwAppInfo, pbwFile) ?: error("JS file not found in PBW")
}