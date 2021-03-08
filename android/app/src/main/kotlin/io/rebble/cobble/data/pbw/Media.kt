package io.rebble.cobble.data.pbw


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.rebble.cobble.pigeons.Pigeons

@JsonClass(generateAdapter = true)
data class Media(
        @Json(name = "file")
        val resourceFile: String,
        val menuIcon: Boolean = false,
        val name: String,
        val type: String
)


fun Media.toPigeon(): Pigeons.WatchResource {
    return Pigeons.WatchResource().also {
        it.file = resourceFile
        it.menuIcon = menuIcon
        it.name = name
        it.type = type
    }
}