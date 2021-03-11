package io.rebble.cobble.data.pbw.manifest


import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SdkVersion(
        val major: Int?,
        val minor: Int?
)