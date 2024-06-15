package io.rebble.cobble.data

import kotlinx.serialization.Serializable

@Serializable
data class NotificationMessage(val sender: String, val text: String, val timestamp: Long)