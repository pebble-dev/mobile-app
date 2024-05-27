package io.rebble.cobble.bluetooth.ble.util

import kotlin.math.min

fun ByteArray.chunked(size: Int): List<ByteArray> {
    val list = mutableListOf<ByteArray>()
    var i = 0
    while (i < this.size) {
        list.add(this.sliceArray(i until (min(i+size, this.size))))
        i += size
    }
    return list
}