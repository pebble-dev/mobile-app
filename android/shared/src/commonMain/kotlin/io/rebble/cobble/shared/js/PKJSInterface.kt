package io.rebble.cobble.shared.js

interface PKJSInterface {
    fun showSimpleNotificationOnPebble(title: String, notificationText: String)

    /**
     * Get account token
     * Sideloaded apps: hash of token and app UUID
     * Appstore apps: hash of token and developer ID
     * //TODO: offline token
     */
    fun getAccountToken(): String

    /**
     * Get token of the watch for storing settings
     * Sideloaded apps: hash of watch serial and app UUID
     * Appstore apps: hash of watch serial and developer ID
     */
    fun getWatchToken(): String
    fun showToast(toast: String)
    fun showNotificationOnPebble(jsonObjectStringNotificationData: String)

    /**
     * Open a URL e.g. configuration page
     */
    fun openURL(url: String): String
}