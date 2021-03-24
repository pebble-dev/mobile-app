package io.rebble.cobble.data.pbw.appinfo


import com.squareup.moshi.JsonClass
import io.rebble.cobble.pigeons.Pigeons

@JsonClass(generateAdapter = true)
data class Watchapp(
        val watchface: Boolean = false,
        val hiddenApp: Boolean = false,
        val onlyShownOnCommunication: Boolean = false
)

fun Watchapp.toPigeon(): Pigeons.WatchappInfo {
    return Pigeons.WatchappInfo().also {
        it.watchface = watchface
        it.hiddenApp = hiddenApp
        it.onlyShownOnCommunication = onlyShownOnCommunication
    }
}