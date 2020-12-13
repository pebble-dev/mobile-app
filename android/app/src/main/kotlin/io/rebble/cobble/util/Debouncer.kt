package io.rebble.cobble.util

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Class that will only execute passed task if a particular time span has been passed without
 * another task being added.
 */
class Debouncer(
        private val debouncingTimeMs: Long = 500L,
        private val scope: CoroutineScope = GlobalScope,
        private val targetContext: CoroutineContext = Dispatchers.Main.immediate
) {
    private var previousJob: Job? = null

    fun executeDebouncing(task: suspend () -> Unit) {
        previousJob?.cancel()
        previousJob = scope.launch(targetContext) {
            delay(debouncingTimeMs)
            task()
        }
    }
}