package io.rebble.cobble.data.pbw.appinfo


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
        // If list of target platforms is not present, pbw is legacy applite app
        val targetPlatforms: List<String> = listOf("aplite"),
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