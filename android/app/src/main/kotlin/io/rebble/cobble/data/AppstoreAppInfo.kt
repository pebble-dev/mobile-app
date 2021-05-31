package io.rebble.cobble.data


import com.squareup.moshi.JsonClass
import io.rebble.cobble.data.pbw.appinfo.Watchapp
import io.rebble.cobble.data.pbw.appinfo.toPigeon
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.pigeons.toMapExt

@JsonClass(generateAdapter = true)
data class AppstoreAppInfo(
        val id: String,
        val uuid: String,
        val title: String = "",
        val list_image: String?,
        val icon_image: String?,
        val screenshot_image: String?,
        val type: String,
)

fun AppstoreAppInfo.toPigeon(): Pigeons.AppstoreAppInfo {
    return Pigeons.AppstoreAppInfo().also {
        it.id = id
        it.uuid = uuid
        it.title = title
        it.list_image = list_image
        it.icon_image = icon_image
        it.screenshot_image = screenshot_image
        it.type = type
    }
}
