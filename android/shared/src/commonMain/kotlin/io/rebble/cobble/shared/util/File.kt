package io.rebble.cobble.shared.util

expect class File(uri: String) {
    fun exists(): Boolean
}