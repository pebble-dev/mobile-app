package io.rebble.fossil.datasources

import android.content.Context
import android.util.Base64
import io.rebble.fossil.util.macAddressToString
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import javax.inject.Inject

class PairedStorage @Inject constructor(private val context: Context) {
    fun getMacAddressOfDefaultPebble(): String? {
        val preferences = context.getSharedPreferences(
                "FlutterSharedPreferences",
                Context.MODE_PRIVATE
        )

        // Read flutter's shared preferences
        // Reference: https://github.com/flutter/plugins/blob/c696333a93d00eb2be2593ca2bcfee235bfc8936/packages/shared_preferences/shared_preferences/android/src/main/java/io/flutter/plugins/sharedpreferences/MethodCallHandlerImpl.java

        val pairListRaw = preferences.getString("flutter.pairList", null)
                ?: return null

        val pairListBase64 = pairListRaw.removePrefix("VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIGxpc3Qu")

        val bytes = Base64.decode(pairListBase64, 0)

        @Suppress("UNCHECKED_CAST")
        val deviceJsons = ObjectInputStream(ByteArrayInputStream(bytes)).use {
            it.readObject()
        } as List<String>

        val devices = deviceJsons.map { JSONObject(it) }

        try {
            val dev = devices.first {
                it.getBoolean("isDefault")
            }
            return dev.getJSONObject("device").getLong("address").macAddressToString()
        } catch (e: NoSuchElementException) {
            return null
        }
    }
}