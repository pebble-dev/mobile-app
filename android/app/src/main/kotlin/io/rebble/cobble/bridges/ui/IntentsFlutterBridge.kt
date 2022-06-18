package io.rebble.cobble.bridges.ui

import android.content.Intent
import io.flutter.plugin.common.BinaryMessenger
import io.rebble.cobble.MainActivity
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.pigeons.BooleanWrapper
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.pigeons.toMapExt
import io.rebble.cobble.util.launchPigeonResult
import io.rebble.cobble.util.registerAsyncPigeonCallback
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class IntentsFlutterBridge @Inject constructor(
        binaryMessenger: BinaryMessenger,
        mainActivity: MainActivity,
        private val coroutineScope: CoroutineScope,
        bridgeLifecycleController: BridgeLifecycleController
) : FlutterBridge, Pigeons.IntentControl {

    private val oauthTrigger = CompletableDeferred<Array<String?>>()
    private val intentCallbacks: Pigeons.IntentCallbacks

    private var flutterReadyToReceiveIntents = false
    private var waitingIntent: Intent? = null

    init {
        mainActivity.oauthIntentCallback = { code, state, error ->
            oauthTrigger.complete(arrayOf(code, state, error))
            mainActivity.oauthIntentCallback = null
        }

        mainActivity.intentCallback = this::forwardIntentToFlutter

        bridgeLifecycleController.setupControl(Pigeons.IntentControl::setup, this)
        intentCallbacks = bridgeLifecycleController.createCallbacks(Pigeons::IntentCallbacks)
    }

    private fun forwardIntentToFlutter(intent: Intent) {
        if (!flutterReadyToReceiveIntents) {
            waitingIntent = intent
            return
        }

        val uri = intent.data?.toString() ?: return
        intentCallbacks.openUri(Pigeons.StringWrapper().also { it.value = uri }) {}
    }

    override fun notifyFlutterReadyForIntents() {
        flutterReadyToReceiveIntents = true
        waitingIntent?.let { forwardIntentToFlutter(it) }
    }

    override fun notifyFlutterNotReadyForIntents() {
        flutterReadyToReceiveIntents = false
    }

    override fun waitForOAuth(result: Pigeons.Result<Pigeons.OAuthResult>?) {
        coroutineScope.launchPigeonResult(result!!, coroutineScope.coroutineContext) {
            val res = oauthTrigger.await()
            check(res.size == 3)
            if (res[0] != null && res[1] != null) {
                Pigeons.OAuthResult.Builder()
                        .setCode(res[0])
                        .setState(res[1])
                        .build()
            }else if (res[3] != null) {
                Pigeons.OAuthResult.Builder()
                        .setError(res[3])
                        .build()
            }else {
                Pigeons.OAuthResult.Builder()
                        .setError("_invalid_callback_params")
                        .build()
            }
        }
    }
}