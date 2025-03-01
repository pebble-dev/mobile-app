package io.rebble.cobble.shared.util

import io.ktor.utils.io.ByteReadChannel

expect class File(uri: String) {
    fun exists(): Boolean

    fun readChannel(): ByteReadChannel
}