package io.rebble.cobble.data.pbw.manifest


import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Resources(
        val name: String?
)