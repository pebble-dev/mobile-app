package io.rebble.cobble.shared.errors

import io.rebble.cobble.shared.Logging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

/**
 * Global exception handler for all coroutines in the app
 */
class GlobalExceptionHandler: CoroutineExceptionHandler {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        if (exception is CancellationException) {
            return
        }

        // TODO properly handle exceptions (logging?)
        Logging.e("Coroutine exception - context: $context", exception)
    }

    override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler
}