package io.rebble.cobble.util

fun String.macAddressToLong(): Long {
    return replace(":", "").toLong(16)
}

fun Long.macAddressToString(): String {
    val hex = "%X".format(this).padStart(12, '0')
    var btaddr = ""
    for (i in hex.indices) {
        btaddr += hex[i]
        if ((i + 1) % 2 == 0 && i + 1 < hex.length) btaddr += ":"
    }

    return btaddr
}