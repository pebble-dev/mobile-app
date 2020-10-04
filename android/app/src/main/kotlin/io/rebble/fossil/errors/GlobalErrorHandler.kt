package io.rebble.fossil.errors

import kotlinx.coroutines.CoroutineExceptionHandler
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Global exception handler for all coroutines in the app
 */
class GlobalExceptionHandler @Inject constructor() : CoroutineExceptionHandler {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        // TODO properly handle exceptions (logging?)
        Timber.e(exception, "Coroutine exception")
    }

    override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler
}