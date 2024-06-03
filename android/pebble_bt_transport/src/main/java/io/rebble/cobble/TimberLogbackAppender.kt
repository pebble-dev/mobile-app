package io.rebble.cobble

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.UnsynchronizedAppenderBase
import timber.log.Timber

class TimberLogbackAppender: UnsynchronizedAppenderBase<ILoggingEvent>() {
    override fun append(eventObject: ILoggingEvent?) {
        if (eventObject == null) {
            return
        }

        val message = eventObject.formattedMessage
        val throwable = Throwable(
                message = eventObject.throwableProxy?.message,
                cause = eventObject.throwableProxy?.cause?.let {
                    Throwable(
                            message = it.message
                    )
                }
        )

        when (eventObject.level.toInt()) {
            Level.TRACE_INT -> {
                if (throwable != null) {
                    Timber.tag(eventObject.loggerName).v(throwable, message)
                } else {
                    Timber.tag(eventObject.loggerName).v(message)
                }
            }
            Level.DEBUG_INT -> {
                if (throwable != null) {
                    Timber.tag(eventObject.loggerName).d(throwable, message)
                } else {
                    Timber.tag(eventObject.loggerName).d(message)
                }
            }
            Level.INFO_INT -> {
                if (throwable != null) {
                    Timber.tag(eventObject.loggerName).i(throwable, message)
                } else {
                    Timber.tag(eventObject.loggerName).i(message)
                }
            }
            Level.WARN_INT -> {
                if (throwable != null) {
                    Timber.tag(eventObject.loggerName).w(throwable, message)
                } else {
                    Timber.tag(eventObject.loggerName).w(message)
                }
            }
            Level.ERROR_INT -> {
                if (throwable != null) {
                    Timber.tag(eventObject.loggerName).e(throwable, message)
                } else {
                    Timber.tag(eventObject.loggerName).e(message)
                }
            }
        }
    }
}