package io.rebble.cobble.shared.util

fun String.encodeToByteArrayTrimmed(maxBytes: Int): ByteArray {
    check(maxBytes >= 2) {
        "maxBytes must be at least 2 to fit ellipsis character. Got $maxBytes instead"
    }

    val encodedOriginal = encodeToByteArray()
    if (encodedOriginal.size <= maxBytes) {
        return encodedOriginal
    }

    var trimmedString = take(maxBytes - 1)
    var encoded = "$trimmedString…".encodeToByteArray()

    while (encoded.size > maxBytes) {
        trimmedString = trimmedString.take(trimmedString.length - 1)
        encoded = "$trimmedString…".encodeToByteArray()
    }

    return encoded
}

fun String.trimWithEllipsis(maxLength: Int): String {
    if (length <= maxLength) {
        return this
    }
    return take(maxLength - 1) + "…"
}