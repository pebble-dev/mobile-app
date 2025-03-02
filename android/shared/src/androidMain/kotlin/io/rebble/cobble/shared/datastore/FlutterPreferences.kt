package io.rebble.cobble.shared.datastore

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import io.rebble.cobble.shared.workarounds.WorkaroundDescriptor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream

/**
 * Read only provider for all shared preferences from flutter
 */
class FlutterPreferences : KoinComponent {
    private val context: Context by inject()
    private val preferences =
        context.getSharedPreferences(
            "FlutterSharedPreferences",
            Context.MODE_PRIVATE
        )

    val mutePhoneNotificationSounds =
        preferences.flow(
            KEY_MUTE_PHONE_NOTIFICATION_SOUNDS
        ) { prefs: SharedPreferences,
            key: String ->
            prefs.getBoolean(key, false)
        }

    val mutePhoneCallSounds =
        preferences.flow(
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

    val masterNotificationsToggle =
        preferences.flow(
            KEY_MASTER_NOTIFICATION_TOGGLE
        ) { prefs: SharedPreferences,
            key: String ->
            prefs.getBoolean(key, true)
        }

    val mutedNotifPackages =
        preferences.flow(
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

private const val KEY_MUTE_PHONE_NOTIFICATION_SOUNDS = "flutter.MUTE_PHONE_NOTIFICATIONS"
private const val KEY_MUTE_PHONE_CALL_SOUNDS = "flutter.MUTE_PHONE_CALLS"
private const val KEY_MASTER_NOTIFICATION_TOGGLE = "flutter.MASTER_NOTIFICATION_TOGGLE"
private const val KEY_PREFIX_DISABLE_WORKAROUND = "flutter.DISABLE_WORKAROUND_"
private const val KEY_MUTED_NOTIF_PACKAGES = "flutter.MUTED_NOTIF_PACKAGES"

private const val LIST_IDENTIFIER = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIGxpc3Qu"