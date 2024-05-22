package io.rebble.cobble.bluetooth.ble.util

fun ByteArray.chunked(size: Int): List<ByteArray> {
    val list = mutableListOf<ByteArray>()
    var i = 0
    while (i < this.size) {
        list.add(this.sliceArray(i until (i + size)))
        i += size
    }
    return list
}