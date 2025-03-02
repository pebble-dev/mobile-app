package io.rebble.cobble.shared.util

import android.net.Uri
import androidx.core.net.toFile
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel

actual class File actual constructor(uri: String) {
    val file = Uri.parse(uri).toFile()

    actual fun exists(): Boolean = file.exists()

    actual fun readChannel(): ByteReadChannel = file.inputStream().toByteReadChannel()
}

fun java.io.File.toKMPFile(): File {
    return File(this.toURI().toString())
}