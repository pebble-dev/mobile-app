package io.rebble.cobble.util

fun ByteArray.toHexString() = asUByteArray().toHexString()
fun UByteArray.toHexString() = joinToString("") { it.toString(16).padStart(2, '0') }


fun BooleanArray.toBytes(): ByteArray {
    val bArr = ByteArray((this.size + 7) / 8)
    for (i in this.indices) {
        val i2 = i / 8
        val i3 = i % 8
        if (this[i]) {
            bArr[i2] = (1 shl i3 or bArr[i2].toInt()).toByte()
        }
    }
    return bArr
}