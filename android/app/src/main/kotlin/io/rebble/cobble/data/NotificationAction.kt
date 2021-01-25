package io.rebble.cobble.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationAction(val title: String, val isResponse: Boolean)