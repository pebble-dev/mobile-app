package io.rebble.cobble.shared.util

import android.net.Uri
import androidx.core.net.toFile

actual class File actual constructor(uri: String) {
    val file = Uri.parse(uri).toFile()
    actual fun exists(): Boolean = file.exists()
}

fun java.io.File.toKMPFile(): File {
    return File(this.toURI().toString())
}