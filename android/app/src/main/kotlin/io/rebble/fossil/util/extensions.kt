package io.rebble.fossil.util

@ExperimentalUnsignedTypes
fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
infix fun Byte.shr(bitCount: Int): Byte = ((this.toInt()) shr bitCount).toByte()
infix fun Byte.shl(bitCount: Int): Byte = ((this.toInt()) shl bitCount).toByte()

infix fun Short.shl(bitCount: Int): Short = ((this.toInt()) shl bitCount).toShort()

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