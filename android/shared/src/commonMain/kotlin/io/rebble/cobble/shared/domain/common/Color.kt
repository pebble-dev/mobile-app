package io.rebble.cobble.shared.domain.common

/**
 *
 */
data class Color(
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Int
)

fun Color.toProtocolNumber() =
    ((alpha / 85) shl 6) or ((red / 85) shl 4) or ((green / 85) shl 2) or (blue / 85)