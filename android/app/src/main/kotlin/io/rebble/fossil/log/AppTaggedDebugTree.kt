package io.rebble.fossil.log

import timber.log.Timber

/**
 * A [timber.log.Timber.Tree] that sets tag to app name and includes actual tag in the message.
 * This is useful for viewing logs on the phone as most logcat apps do not support per-app filtering.
 *
 * Taken from https://github.com/matejdro/WearUtils/blob/011964110a541126e3639172dc81b72ccf836a27/src/main/java/timber/log/Timber.java
 */
open class AppTaggedDebugTree(private val appTag: String) : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, appTag, "[$tag] $message", t)
    }
}