package io.rebble.cobble.bluetooth.workarounds

import android.content.Context
import android.os.Build
import io.rebble.cobble.bluetooth.workarounds.UnboundWatchBeforeConnecting.isNeeded

/**
 * Workaround for BT stack bug on Android 9 and 10 where phone can't connect to the watch if it was
 * already paired. Connection only works if watch just paired.
 *
 * As a workaround, we unpair watch before every connection.
 *
 * Side effects:
 * * Produces annoying pairing prompts on the watch
 *
 * This might be fixed on some devices. We should update [isNeeded] with checks for those
 * devices if we can make a whitelist.
 *
 * See https://issuetracker.google.com/issues/144057796 for more info
 */
object UnboundWatchBeforeConnecting : WorkaroundDescriptor {
    override val name: String
        get() = "UBOUND_WATCH_BEFORE_CONNECTING"

    override fun isNeeded(context: Context): Boolean {
        return Build.VERSION.SDK_INT in Build.VERSION_CODES.P..Build.VERSION_CODES.Q
    }
}