package io.rebble.cobble.shared.domain.api.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class RWSAccount(
    val uid: Long,
    val name: String,
    @SerialName("is_subscribed")
    val isSubscribed: Boolean,
    val scopes: List<String>,
    @SerialName("is_wizard")
    val isWizard: Boolean,
    @SerialName("has_timeline")
    val hasTimeline: Boolean,
    @SerialName("timeline_ttl")
    val timelineTtl: Int,
    @SerialName("boot_overrides")
    val bootOverrides: JsonObject?
)