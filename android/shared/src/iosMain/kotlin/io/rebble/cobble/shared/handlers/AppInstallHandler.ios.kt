package io.rebble.cobble.shared.handlers

import io.ktor.utils.io.ByteReadChannel
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.util.File

actual fun getAppPbwFile(
    context: PlatformContext,
    appUuid: String
): File {
    TODO("Not yet implemented")
}

actual suspend fun savePbwFile(
    context: PlatformContext,
    appUuid: String,
    byteReadChannel: ByteReadChannel
): String {
    TODO("Not yet implemented")
}