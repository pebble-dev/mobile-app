package io.rebble.cobble.shared.handlers

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.cio.use
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.copyAndClose
import io.rebble.cobble.shared.AndroidPlatformContext
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.database.dao.LockerDao
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

actual suspend fun downloadPbw(context: PlatformContext, httpClient: HttpClient, lockerDao: LockerDao, appUuid: String): String? {
    val row = lockerDao.getEntryByUuid(appUuid)
    val url = row?.entry?.pbwLink ?: run {
        Logging.e("App URL for $appUuid not found in locker")
        return null
    }

    val response = httpClient.get(url)
    if (response.status.value != 200) {
        Logging.e("Failed to download app $appUuid: ${response.status}")
        return null
    } else {
        return savePbwFile(context, appUuid, response.bodyAsChannel())
    }
}