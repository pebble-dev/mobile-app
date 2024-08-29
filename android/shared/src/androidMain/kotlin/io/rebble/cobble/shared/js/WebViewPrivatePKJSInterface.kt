package io.rebble.cobble.shared.js

import android.net.Uri
import android.webkit.JavascriptInterface
import io.rebble.cobble.shared.Logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class WebViewPrivatePKJSInterface(private val jsRunner: WebViewJsRunner, private val scope: CoroutineScope): PrivatePKJSInterface {

    @JavascriptInterface
    override fun privateLog(message: String) {
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    override fun logInterceptedSend() {
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    override fun logInterceptedRequest() {
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    override fun getVersionCode(): Int {
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    fun startupScriptHasLoaded(url: String) {
        Logging.d("Startup script has loaded: $url")
        val uri = Uri.parse(url)
        val params = uri.getQueryParameter("params")
        scope.launch {
            jsRunner.loadAppJs(params)
        }
    }

    @JavascriptInterface
    fun privateFnLocalStorageWrite(key: String, value: String) {
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    fun privateFnLocalStorageRead(key: String): String {
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    fun privateFnLocalStorageReadAll(): String {
        return "{}"
    }

    @JavascriptInterface
    fun privateFnLocalStorageReadAll_AtPreregistrationStage(baseUriReference: String): String {
        return privateFnLocalStorageReadAll()
    }

    @JavascriptInterface
    fun signalAppScriptLoadedByBootstrap() {
        scope.launch {
            jsRunner.signalReady()
        }
    }
}