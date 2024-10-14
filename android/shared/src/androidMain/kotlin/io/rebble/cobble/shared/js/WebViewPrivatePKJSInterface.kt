package io.rebble.cobble.shared.js

import android.net.Uri
import android.webkit.JavascriptInterface
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.data.js.ActivePebbleWatchInfo
import io.rebble.cobble.shared.data.js.fromDevice
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WebViewPrivatePKJSInterface(private val jsRunner: WebViewJsRunner, private val scope: CoroutineScope, private val outgoingAppMessages: MutableSharedFlow<String>): PrivatePKJSInterface {

    @JavascriptInterface
    override fun privateLog(message: String) {
        Logging.v("privateLog: $message")
    }

    @JavascriptInterface
    override fun logInterceptedSend() {
        Logging.v("logInterceptedSend")
    }

    @JavascriptInterface
    override fun logInterceptedRequest() {
        Logging.v("logInterceptedRequest")
    }

    @JavascriptInterface
    override fun getVersionCode(): Int {
        Logging.v("getVersionCode")
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    override fun logLocationRequest() {
        Logging.v("logLocationRequest")
    }

    @JavascriptInterface
    fun startupScriptHasLoaded(url: String) {
        Logging.v("Startup script has loaded: $url")
        val uri = Uri.parse(url)
        val params = uri.getQueryParameter("params")
        scope.launch {
            jsRunner.loadAppJs(params)
        }
    }

    @JavascriptInterface
    fun privateFnLocalStorageWrite(key: String, value: String) {
        Logging.v("privateFnLocalStorageWrite")
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    fun privateFnLocalStorageRead(key: String): String {
        Logging.v("privateFnLocalStorageRead")
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    fun privateFnLocalStorageReadAll(): String {
        Logging.v("privateFnLocalStorageReadAll")
        return "{}"
    }

    @JavascriptInterface
    fun privateFnLocalStorageReadAll_AtPreregistrationStage(baseUriReference: String): String {
        Logging.v("privateFnLocalStorageReadAll_AtPreregistrationStage")
        return privateFnLocalStorageReadAll()
    }

    @JavascriptInterface
    fun signalAppScriptLoadedByBootstrap() {
        Logging.v("signalAppScriptLoadedByBootstrap")
        scope.launch {
            jsRunner.signalReady()
        }
    }

    @JavascriptInterface
    fun sendAppMessageString(jsonAppMessage: String) {
        Logging.v("sendAppMessageString")
        if (!outgoingAppMessages.tryEmit(jsonAppMessage)) {
            Logging.e("Failed to emit outgoing AppMessage")
        }
    }

    @JavascriptInterface
    fun getActivePebbleWatchInfo(): String {
        return ConnectionStateManager.connectionState.value.watchOrNull?.let {
            Json.encodeToString(ActivePebbleWatchInfo.fromDevice(it))
        } ?: error("No active watch")
    }
}