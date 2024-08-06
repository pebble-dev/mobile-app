package io.rebble.cobble.shared.js

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast
import io.rebble.cobble.shared.Logging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WebViewPKJSInterface(private val jsRunner: JsRunner): PKJSInterface, KoinComponent {
    val context: Context by inject()

    @JavascriptInterface
    override fun showSimpleNotificationOnPebble(title: String, notificationText: String) {
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    override fun getAccountToken(): String {
        //TODO
        return ""
    }

    @JavascriptInterface
    override fun getWatchToken(): String {
        //TODO
        return ""
    }

    @JavascriptInterface
    override fun showToast(toast: String) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    override fun showNotificationOnPebble(jsonObjectStringNotificationData: String) {
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    override fun openURL(url: String): String {
        Logging.d("Opening URL")
        jsRunner.loadUrl(url)
        return url
    }
}