package io.rebble.cobble.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationMessage (val sender: String, val text: String, val timestamp: Long)