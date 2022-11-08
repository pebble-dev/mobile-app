package io.rebble.cobble.util

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Class that will only execute passed task if a particular time span has been passed without
 * another task being added.
 *
 * When [triggerFirstImmediately] flag is set, task will be triggered immediately if there was
 * no other task within [debouncingTimeMs]. Otherwise, it will wait for another [debouncingTimeMs]
 * to ensure no further tasks are there.
 */
class Debouncer(
        private val debouncingTimeMs: Long = 500L,
        private val triggerFirstImmediately: Boolean = false,
        private val scope: CoroutineScope = GlobalScope,
        private val targetContext: CoroutineContext = Dispatchers.Main.immediate
) {
    private var previousJob: Job? = null
    private var lastStart = 0L

    fun executeDebouncing(task: suspend () -> Unit) {
        previousJob?.cancel()

        previousJob = scope.launch(targetContext) {
            if (!triggerFirstImmediately ||
                    (System.currentTimeMillis() - lastStart) < debouncingTimeMs) {
                delay(debouncingTimeMs)
            }

            lastStart = System.currentTimeMillis()
            task()
        }
    }
}