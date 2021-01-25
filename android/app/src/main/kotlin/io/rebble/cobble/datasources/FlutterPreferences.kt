package io.rebble.cobble.datasources

import android.content.Context
import android.content.SharedPreferences
import dagger.Reusable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * Read only provider for all shared preferences from flutter
 */
@Reusable
class FlutterPreferences @Inject constructor(context: Context) {
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
}

@OptIn(ExperimentalCoroutinesApi::class)
private inline fun <T> SharedPreferences.flow(
        key: String,
        crossinline mapper: (preferences: SharedPreferences, key: String) -> T): Flow<T> {

    return callbackFlow {
        offer(mapper(this@flow, key))

        val listener = SharedPreferences
                .OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences,
                                                    changedKey: String ->

                    if (changedKey == key) {
                        offer(mapper(sharedPreferences, key))
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