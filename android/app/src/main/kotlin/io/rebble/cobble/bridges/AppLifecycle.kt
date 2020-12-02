package io.rebble.cobble.bridges

import io.flutter.plugin.common.BinaryMessenger
import io.rebble.cobble.MainActivity
import io.rebble.cobble.pigeons.BooleanWrapper
import io.rebble.cobble.pigeons.toMapExt
import io.rebble.cobble.util.registerAsyncPigeonCallback
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class AppLifecycle @Inject constructor(
        binaryMessenger: BinaryMessenger,
        mainActivity: MainActivity,
        coroutineScope: CoroutineScope
) : FlutterBridge {
    private val bootTrigger = CompletableDeferred<Boolean>()

    init {
        mainActivity.bootIntentCallback = {
            bootTrigger.complete(it)
            mainActivity.bootIntentCallback = null
        }

        binaryMessenger.registerAsyncPigeonCallback(
                coroutineScope,
                "dev.flutter.pigeon.AppLifecycleControl.waitForBoot"
        ) {
            BooleanWrapper(bootTrigger.await()).toMapExt()
        }
    }
}