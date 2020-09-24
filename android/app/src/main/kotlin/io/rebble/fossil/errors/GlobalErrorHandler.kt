package io.rebble.fossil.errors

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Global exception handler for all coroutines in the app
 */
class GlobalExceptionHandler @Inject constructor() : CoroutineExceptionHandler {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        // TODO properly handle exceptions (logging?)
        Log.e("Fossil", "Coroutine exception", exception)
    }

    override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler
}