package io.rebble.cobble.bridges.ui

import io.flutter.plugin.common.BinaryMessenger
import io.rebble.cobble.di.PerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job
import javax.inject.Inject

@PerActivity
/**
 * Helper that automatically closes down all pigeon bridges when activity is destroyed
 */
class BridgeLifecycleController @Inject constructor(
        private val binaryMessenger: BinaryMessenger,
        coroutineScope: CoroutineScope
) {
    private val activatedSetupMethods = ArrayList<(BinaryMessenger, Any?) -> Unit>()

    fun <T> setupControl(pigeonSetupMethod: (BinaryMessenger, T) -> Unit, callback: T) {
        pigeonSetupMethod(binaryMessenger, callback)
        @Suppress("UNCHECKED_CAST")
        activatedSetupMethods.add(pigeonSetupMethod as (BinaryMessenger, Any?) -> Unit)
    }

    fun <T> createCallbacks(pigeonSetupMethod: (BinaryMessenger) -> T): T {
        return pigeonSetupMethod(binaryMessenger)
    }

    init {
        coroutineScope.coroutineContext.job.invokeOnCompletion {
            for (setupMethod in activatedSetupMethods) {
                setupMethod.invoke(binaryMessenger, null)
            }
        }
    }
}