package io.rebble.cobble.data.pbw.manifest


import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PbwManifest(
        val application: PbwBlob,
        val resources: PbwBlob?,
        val worker: PbwBlob?,
        val debug: Debug?,
        val generatedAt: Int?,
        val generatedBy: String?,
        val manifestVersion: Int?,
        val type: String?
)