package io.rebble.cobble.util

import okio.Source
import okio.source
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Find specific entry in this ZIP stream.
 *
 * After entry is found, ZIP stream will be positioned to
 * read from this entry.
 */
inline fun ZipInputStream.findEntry(predicate: (ZipEntry) -> Boolean): ZipEntry? {
    var entry: ZipEntry? = this.nextEntry

    while (entry != null) {
        if (predicate(entry)) {
            return entry
        }

        closeEntry()
        entry = this.nextEntry
    }

    return null
}

fun ZipInputStream.findFile(fileName: String): ZipEntry? {
    return findEntry {
        it.name == fileName
    }
}

/**
 * Returns [Source] of the file named [fileName] inside this zip file or *null* if file is not
 * valid zip file or does not contain [fileName].
 */
fun File.zippedSource(fileName: String): Source? {
    val zipInputStream = ZipInputStream(inputStream())

    zipInputStream.findFile(fileName) ?: return null

    return zipInputStream.source()
}

fun InputStream.zippedSource(fileName: String): Source? {
    val zipInputStream = ZipInputStream(this)

    zipInputStream.findFile(fileName) ?: return null

    return zipInputStream.source()
}