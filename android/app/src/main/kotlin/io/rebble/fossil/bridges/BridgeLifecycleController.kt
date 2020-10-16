package io.rebble.fossil.bridges

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.flutter.plugin.common.BinaryMessenger
import io.rebble.fossil.di.PerActivity
import javax.inject.Inject

@PerActivity
/**
 * Helper that automatically closes down all pigeon bridges when activity is destroyed
 */
class BridgeLifecycleController @Inject constructor(
        private val binaryMessenger: BinaryMessenger,
        lifecycle: Lifecycle
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
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    for (setupMethod in activatedSetupMethods) {
                        setupMethod.invoke(binaryMessenger, null)
                    }
                }
            }
        })
    }
}