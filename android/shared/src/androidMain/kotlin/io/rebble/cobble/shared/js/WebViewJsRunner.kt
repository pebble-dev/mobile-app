package io.rebble.cobble.shared.js

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Message
import android.view.View
import android.webkit.*
import io.rebble.cobble.shared.Logging
import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WebViewJsRunner(val context: Context, private val scope: CoroutineScope, appInfo: PbwAppInfo, jsPath: String): JsRunner(appInfo, jsPath) {

    companion object {
        const val API_NAMESPACE = "Pebble"
        const val PRIVATE_API_NAMESPACE = "_$API_NAMESPACE"
        const val STARTUP_URL = "file:///android_asset/webview_startup.html"
    }

    private var webView: WebView? = null
    private val initializedLock = Object()
    private val publicJsInterface = WebViewPKJSInterface()
    private val privateJsInterface = WebViewPrivatePKJSInterface(this, scope)
    private val interfaces = setOf(
            Pair(API_NAMESPACE, publicJsInterface),
            Pair(PRIVATE_API_NAMESPACE, privateJsInterface)
    )

    private val webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Logging.d("Page finished loading: $url")
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            Logging.e("Error loading page: ${error?.errorCode} ${error?.description}")
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            super.onReceivedSslError(view, handler, error)
            Logging.e("SSL error loading page: ${error?.primaryError}")
            handler?.cancel()
        }

        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            if (isForbidden(request?.url)) {
                return object : WebResourceResponse("text/plain", "utf-8", null) {
                    override fun getStatusCode(): Int {
                        return 403
                    }

                    override fun getReasonPhrase(): String {
                        return "Forbidden"
                    }
                }
            } else {
                return super.shouldInterceptRequest(view, request)
            }
        }
    }

    private fun isForbidden(url: Uri?): Boolean {
        return if (url == null) {
            Logging.w("Blocking null URL")
            true
        } else if (url.scheme?.uppercase() != "FILE") {
            false
        } else if (url.path?.uppercase() == jsPath.uppercase()) {
            false
        } else {
            Logging.w("Blocking access to file: ${url.path}")
            true
        }
    }

    private val chromeClient = object : WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            //TODO: forward to developer log
            consoleMessage?.let {
                Logging.d("WebView: ${it.message()}")
            }
            return false
        }

        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
            return false
        }

        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            return false
        }

        override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            return false
        }

        override fun onJsBeforeUnload(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            return false
        }

        override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
            return false
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            //Stub
        }

        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
            return false
        }

        override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
            Logging.d("Geolocation permission requested for $origin")
            callback?.invoke(origin, true, false)
        }

        override fun onPermissionRequest(request: PermissionRequest?) {
            request?.deny()
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private suspend fun init() = withContext(Dispatchers.Main) {
        webView = WebView(context).also {
            it.setWillNotDraw(true)
            val settings = it.settings
            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = false

            //TODO: use WebViewAssetLoader instead
            settings.allowUniversalAccessFromFileURLs = true
            settings.allowFileAccessFromFileURLs = true

            settings.databaseEnabled = true
            settings.domStorageEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            it.clearCache(true)

            interfaces.forEach { (namespace, jsInterface) ->
                it.addJavascriptInterface(jsInterface, namespace)
            }
            webView?.webViewClient = webViewClient
            webView?.webChromeClient = chromeClient
        }
    }


    override suspend fun start() {
        synchronized(initializedLock) {
            check(webView == null) { "WebviewJsRunner already started" }
        }
        try {
            init()
        } catch (e: Exception) {
            synchronized(initializedLock) {
                webView = null
            }
            throw e
        }
        check(webView != null) { "WebView not initialized" }
        loadApp(jsPath)
    }

    override suspend fun stop() {
        //TODO: Close config screens

        withContext(Dispatchers.Main) {
            interfaces.forEach { (namespace, _) ->
                webView?.removeJavascriptInterface(namespace)
            }
            webView?.loadUrl("about:blank")
            webView?.stopLoading()
            webView?.clearHistory()
            webView?.removeAllViews()
            webView?.clearCache(true)
            webView?.destroy()
        }
        synchronized(initializedLock) {
            webView = null
        }
    }

    private suspend fun loadApp(url: String) {
        check(webView != null) { "WebView not initialized" }
        withContext(Dispatchers.Main) {
            webView?.loadUrl(
                    Uri.parse(STARTUP_URL).buildUpon()
                            .appendQueryParameter("params", "{\"loadUrl\": \"$url\"}")
                            .build()
                            .toString()
            )
        }
    }

    suspend fun loadAppJs(params: String?) {
        check(webView != null) { "WebView not initialized" }
        if (params == null) {
            Logging.e("No params passed to loadAppJs")
            return
        }

        
    }
}