package io.rebble.cobble.bluetooth.ble.util

import kotlin.math.min

fun ByteArray.chunked(maxChunkSize: Int): List<ByteArray> {
    require(maxChunkSize > 0) { "Chunk size must be greater than 0" }
    val chunks = mutableListOf<ByteArray>()
    var offset = 0
    while (offset < size) {
        val chunkSize = min(maxChunkSize, size - offset)
        chunks.add(copyOfRange(offset, offset + chunkSize))
        offset += chunkSize
    }
    return chunks
}