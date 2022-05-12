package io.rebble.cobble.data

import kotlinx.serialization.Serializable

@Serializable
data class NotificationAction(val title: String, val isResponse: Boolean)