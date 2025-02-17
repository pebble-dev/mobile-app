package io.rebble.cobble.shared.data

data class WatchItem(
        val name: String,
        val softwareVersion: String,
        val isConnected: Boolean,
        val updateAvailable: Boolean
)