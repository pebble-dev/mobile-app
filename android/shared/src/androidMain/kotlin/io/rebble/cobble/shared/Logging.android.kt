package io.rebble.cobble.shared

import android.util.Log
import timber.log.Timber

actual fun log(
    level: LogLevel,
    message: String,
    throwable: Throwable?
) {
    val caller = Throwable().stackTrace[3]
    Timber.tag(
        "${caller.className.split(".").last()}$${caller.methodName}"
    ).log(level.toTimber(), throwable, message)
}

private fun LogLevel.toTimber(): Int {
    return when (this) {
        LogLevel.VERBOSE -> Log.VERBOSE
        LogLevel.DEBUG -> Log.DEBUG
        LogLevel.INFO -> Log.INFO
        LogLevel.WARNING -> Log.WARN
        LogLevel.ERROR -> Log.ERROR
        LogLevel.ASSERT -> Log.ASSERT
        else -> error("Unknown log level: $this")
    }
}