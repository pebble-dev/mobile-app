package io.rebble.cobble.data.pbw


import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Resources(
        val media: List<Media>
)