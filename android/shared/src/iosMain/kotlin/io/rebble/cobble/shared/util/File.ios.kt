package io.rebble.cobble.shared.util

import io.ktor.utils.io.ByteReadChannel

actual class File actual constructor(uri: String) {
    actual fun exists(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun readChannel(): ByteReadChannel {
        TODO("Not yet implemented")
    }
}