package io.rebble.cobble.datasources

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class AndroidPreferences @Inject constructor(context: Context) {
    private val preferences = context.getSharedPreferences("android", Context.MODE_PRIVATE)

    var backgroundEndpoint: Long?
        get() = preferences.getLongOrNull(KEY_FLUTTER_BACKGROUND_HANDLE)
        set(value) = preferences.edit()
                .putNullableLong(KEY_FLUTTER_BACKGROUND_HANDLE, value)
                .apply()
}

private fun SharedPreferences.getLongOrNull(key: String): Long? {
    return if (contains(key)) {
        getLong(key, 0)
    } else {
        null
    }
}

private fun SharedPreferences.Editor.putNullableLong(key: String, value: Long?): SharedPreferences.Editor {
    if (value != null) {
        putLong(key, value)
    } else {
        remove(key)
    }

    return this
}

private val KEY_FLUTTER_BACKGROUND_HANDLE = "FlutterBackgroundHandle"