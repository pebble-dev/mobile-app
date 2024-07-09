package io.rebble.cobble.shared.js

import android.webkit.JavascriptInterface

class WebViewPKJSInterface: PKJSInterface {
    @JavascriptInterface
    override fun showSimpleNotificationOnPebble(title: String, notificationText: String) {
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    override fun getAccountToken(): String {
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    override fun getWatchToken(): String {
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    override fun showToast(toast: String) {
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    override fun showNotificationOnPebble(jsonObjectStringNotificationData: String) {
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    override fun openURL(url: String): String {
        TODO("Not yet implemented")
    }
}