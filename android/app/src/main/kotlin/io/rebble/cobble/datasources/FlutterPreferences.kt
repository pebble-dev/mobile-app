package io.rebble.cobble.datasources

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import dagger.Reusable
import io.rebble.cobble.bluetooth.workarounds.WorkaroundDescriptor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import javax.inject.Inject

/**
 * Read only provider for all shared preferences from flutter
 */
@Reusable
class FlutterPreferences @Inject constructor(private val context: Context) {
    private val preferences = context.getSharedPreferences(
            "FlutterSharedPreferences",
            Context.MODE_PRIVATE
    )

    val calendarSyncEnabled = preferences.flow(KEY_CALENDAR_SYNC_ENABLED) { prefs: SharedPreferences,
                                                                            key: String ->
        prefs.getBoolean(key, false)
    }

    val mutePhoneNotificationSounds = preferences.flow(
            KEY_MUTE_PHONE_NOTIFICATION_SOUNDS
    ) { prefs: SharedPreferences,
        key: String ->
        prefs.getBoolean(key, false)
    }

    val mutePhoneCallSounds = preferences.flow(
            KEY_MUTE_PHONE_CALL_SOUNDS
    ) { prefs: SharedPreferences,
        key: String ->
        prefs.getBoolean(key, false)
    }

    fun shouldActivateWorkaround(workaround: WorkaroundDescriptor): Boolean {
        if (!workaround.isNeeded(context)) {
            return false
        }

        return !preferences.getBoolean(KEY_PREFIX_DISABLE_WORKAROUND + workaround.name, false)
    }

    val masterNotificationsToggle = preferences.flow(
            KEY_MASTER_NOTIFICATION_TOGGLE
    ) { prefs: SharedPreferences,
        key: String ->
        prefs.getBoolean(key, true)
    }

    val mutedNotifPackages = preferences.flow(
            KEY_MUTED_NOTIF_PACKAGES
    ) { prefs: SharedPreferences,
        key: String ->
        val list = prefs.getString(key, null)
        if (list != null) return@flow decodeList(list) else return@flow null
    }
}

private fun decodeList(encodedList: String): List<String>? {
    var stream: ObjectInputStream? = null
    return try {
        val id = Base64.decode(LIST_IDENTIFIER, 0)
        val bytestream = ByteArrayInputStream(Base64.decode(encodedList, 0))
        bytestream.skip(id.size.toLong())
        stream = ObjectInputStream(bytestream)
        stream.readObject() as? List<String>
    } finally {
        stream?.close()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private inline fun <T> SharedPreferences.flow(
        key: String,
        crossinline mapper: (preferences: SharedPreferences, key: String) -> T): Flow<T> {

    return callbackFlow {
        trySend(mapper(this@flow, key)).isSuccess

        val listener = SharedPreferences
                .OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences,
                                                    changedKey: String? ->

                    if (changedKey == key) {
                        trySend(mapper(sharedPreferences, key)).isSuccess
                    }
                }

        registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}

private const val KEY_CALENDAR_SYNC_ENABLED = "flutter.ENABLE_CALENDAR_SYNC"
private const val KEY_MUTE_PHONE_NOTIFICATION_SOUNDS = "flutter.MUTE_PHONE_NOTIFICATIONS"
private const val KEY_MUTE_PHONE_CALL_SOUNDS = "flutter.MUTE_PHONE_CALLS"
private const val KEY_MASTER_NOTIFICATION_TOGGLE = "flutter.MASTER_NOTIFICATION_TOGGLE"
private const val KEY_PREFIX_DISABLE_WORKAROUND = "flutter.DISABLE_WORKAROUND_"
private const val KEY_MUTED_NOTIF_PACKAGES = "flutter.MUTED_NOTIF_PACKAGES"

private const val LIST_IDENTIFIER = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIGxpc3Qu"