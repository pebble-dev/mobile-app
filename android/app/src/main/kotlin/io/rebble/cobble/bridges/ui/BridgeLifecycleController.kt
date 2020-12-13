package io.rebble.cobble.bridges.ui

import io.flutter.plugin.common.BinaryMessenger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * Helper that automatically closes down all pigeon bridges when activity is destroyed
 */
class BridgeLifecycleController constructor(
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
        coroutineScope.coroutineContext[Job]?.invokeOnCompletion {
            for (setupMethod in activatedSetupMethods) {
                setupMethod.invoke(binaryMessenger, null)
            }
        }
    }
}