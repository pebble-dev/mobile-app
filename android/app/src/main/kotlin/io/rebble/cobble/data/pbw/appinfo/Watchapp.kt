package io.rebble.cobble.data.pbw.appinfo


import io.rebble.cobble.pigeons.Pigeons
import io.rebble.libpebblecommon.metadata.pbw.appinfo.Watchapp

fun Watchapp.toPigeon(): Pigeons.WatchappInfo {
    return Pigeons.WatchappInfo().also {
        it.watchface = watchface
        it.hiddenApp = hiddenApp
        it.onlyShownOnCommunication = onlyShownOnCommunication
    }
}