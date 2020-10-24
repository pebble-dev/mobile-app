package io.rebble.fossil.util

fun ByteArray.toHexString() = asUByteArray().toHexString()
fun UByteArray.toHexString() = joinToString("") { it.toString(16).padStart(2, '0') }

infix fun Byte.ushr(bitCount: Int): Byte = ((this.toInt()) ushr bitCount).toByte()
infix fun Byte.shl(bitCount: Int): Byte = ((this.toInt()) shl bitCount).toByte()
infix fun UByte.shr(bitCount: Int): UByte = ((this.toUInt()) shr bitCount).toUByte()
infix fun UByte.shl(bitCount: Int): UByte = ((this.toUInt()) shl bitCount).toUByte()

infix fun Short.shl(bitCount: Int): Short = ((this.toInt()) shl bitCount).toShort()
infix fun UShort.shl(bitCount: Int): UShort = ((this.toUInt()) shl bitCount).toUShort()

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