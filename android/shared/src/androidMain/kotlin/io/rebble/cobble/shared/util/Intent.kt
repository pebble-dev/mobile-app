package io.rebble.cobble.shared.util

import android.content.Intent

fun Intent.getIntExtraOrNull(key: String): Int? {
    return if (hasExtra(key)) {
        getIntExtra(key, -1)
    } else {
        null
    }
}