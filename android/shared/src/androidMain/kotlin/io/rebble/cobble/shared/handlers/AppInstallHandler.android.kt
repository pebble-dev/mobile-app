package io.rebble.cobble.shared.handlers

import io.ktor.util.cio.use
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.copyAndClose
import io.rebble.cobble.shared.AndroidPlatformContext
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.util.File
import io.rebble.cobble.shared.util.toKMPFile

actual fun getAppPbwFile(context: PlatformContext, appUuid: String): File {
    require(context is AndroidPlatformContext)
    val appsDir = java.io.File(context.applicationContext.filesDir, "apps")
    appsDir.mkdirs()
    val targetFileName = java.io.File(appsDir, "$appUuid.pbw")
    return targetFileName.toKMPFile()
}

actual suspend fun savePbwFile(context: PlatformContext, appUuid: String, byteReadChannel: ByteReadChannel): String {
    val file = getAppPbwFile(context, appUuid)
    file.file.writeChannel().use {
        byteReadChannel.copyAndClose(this)
    }
    return file.file.toURI().toString()
}