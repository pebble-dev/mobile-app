package io.rebble.cobble.errors

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Global exception handler for all coroutines in the app
 */
class GlobalExceptionHandler @Inject constructor() : CoroutineExceptionHandler {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        if (exception is CancellationException) {
            return
        }

        // TODO properly handle exceptions (logging?)
        Timber.e(exception, "Coroutine exception")
    }

    override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler
}