package io.rebble.cobble.data.pbw.manifest


import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PbwManifest(
        val application: Application,
        val debug: Debug?,
        val generatedAt: Int?,
        val generatedBy: String?,
        val manifestVersion: Int?,
        val resources: Resources?,
        val type: String?
)