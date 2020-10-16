package io.rebble.fossil.util

@ExperimentalUnsignedTypes
fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
infix fun Byte.shr(bitCount: Int): Byte = ((this.toInt()) shr bitCount).toByte()
infix fun Byte.shl(bitCount: Int): Byte = ((this.toInt()) shl bitCount).toByte()

infix fun Short.shl(bitCount: Int): Short = ((this.toInt()) shl bitCount).toShort()