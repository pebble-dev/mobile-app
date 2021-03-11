package io.rebble.cobble.data.pbw.manifest


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Application(
        val crc: Long?,
        val name: String,
        @Json(name = "sdk_version")
        val sdkVersion: SdkVersion?,
        val size: Int?,
        val timestamp: Int?
)