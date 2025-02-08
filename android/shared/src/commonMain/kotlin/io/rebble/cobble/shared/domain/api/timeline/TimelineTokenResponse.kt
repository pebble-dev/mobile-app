package io.rebble.cobble.shared.domain.api.timeline

import kotlinx.serialization.Serializable

@Serializable
data class TimelineTokenResponse(
        val token: String,
        val uuid: String
)