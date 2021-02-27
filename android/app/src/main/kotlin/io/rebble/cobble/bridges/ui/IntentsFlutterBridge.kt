package io.rebble.cobble.bridges.ui

import android.content.Intent
import io.flutter.plugin.common.BinaryMessenger
import io.rebble.cobble.MainActivity
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.pigeons.BooleanWrapper
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.pigeons.toMapExt
import io.rebble.cobble.util.registerAsyncPigeonCallback
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class IntentsFlutterBridge @Inject constructor(
        binaryMessenger: BinaryMessenger,
        mainActivity: MainActivity,
        coroutineScope: CoroutineScope,
        bridgeLifecycleController: BridgeLifecycleController
) : FlutterBridge, Pigeons.IntentControl {

    private val bootTrigger = CompletableDeferred<Boolean>()
    private val intentCallbacks: Pigeons.IntentCallbacks

    private var flutterInitialized = false
    private var waitingIntent: Intent? = null

    init {
        mainActivity.bootIntentCallback = {
            bootTrigger.complete(it)
            mainActivity.bootIntentCallback = null
        }

        mainActivity.intentCallback = this::forwardIntentToFlutter

        bridgeLifecycleController.setupControl(Pigeons.IntentControl::setup, this)
        intentCallbacks = bridgeLifecycleController.createCallbacks(Pigeons::IntentCallbacks)

        binaryMessenger.registerAsyncPigeonCallback(
                coroutineScope,
                "dev.flutter.pigeon.IntentControl.waitForBoot"
        ) {
            BooleanWrapper(bootTrigger.await()).toMapExt()
        }
    }

    private fun forwardIntentToFlutter(intent: Intent) {
        if (!flutterInitialized) {
            waitingIntent = intent
            return
        }

        val uri = intent.data?.toString() ?: return
        intentCallbacks.openUri(Pigeons.StringWrapper().also { it.value = uri }) {}
    }

    override fun notifyFlutterReadyForIntents() {
        flutterInitialized = true
        waitingIntent?.let { forwardIntentToFlutter(it) }
    }

    override fun waitForBoot(): Pigeons.BooleanWrapper {
        throw UnsupportedOperationException("This method should never trigger. We override it " +
                "with async handler above")
    }
}