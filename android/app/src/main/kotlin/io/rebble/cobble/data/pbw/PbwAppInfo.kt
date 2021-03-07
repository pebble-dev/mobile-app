package io.rebble.cobble.data.pbw


import com.squareup.moshi.JsonClass
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.pigeons.toMapExt

@JsonClass(generateAdapter = true)
data class PbwAppInfo(
        val uuid: String,
        val shortName: String,
        val longName: String = "",
        val companyName: String = "",
        val versionCode: Long = -1,
        val versionLabel: String,
        val appKeys: Map<String, Int> = emptyMap(),
        val capabilities: List<String> = emptyList(),
        val resources: Resources,
        val sdkVersion: String = "3",
        val targetPlatforms: List<String>,
        val watchapp: Watchapp = Watchapp()
)

fun PbwAppInfo.toPigeon(): Pigeons.PbwAppInfo {
    return Pigeons.PbwAppInfo().also {
        it.uuid = uuid
        it.shortName = shortName
        it.longName = longName
        it.companyName = companyName
        it.versionCode = versionCode
        it.versionLabel = versionLabel
        it.appKeys = HashMap(appKeys)
        it.capabilities = ArrayList(capabilities)
        it.resources = ArrayList(resources.media.map { it.toPigeon().toMapExt() })
        it.targetPlatforms = ArrayList(targetPlatforms)
        it.watchapp = watchapp.toPigeon()

        it.isValid = true

    }
}