package io.rebble.cobble.shared.domain.notifications

import kotlinx.serialization.Serializable

@Serializable
data class NotificationMessage(val sender: String, val text: String, val timestamp: Long)