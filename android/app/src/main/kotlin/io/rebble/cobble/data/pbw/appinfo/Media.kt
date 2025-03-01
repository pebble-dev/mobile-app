package io.rebble.cobble.data.pbw.appinfo

import io.rebble.cobble.pigeons.Pigeons
import io.rebble.libpebblecommon.metadata.pbw.appinfo.Media

fun Media.toPigeon(): Pigeons.WatchResource {
    return Pigeons.WatchResource().also {
        it.file = resourceFile
        it.menuIcon = menuIcon.value
        it.name = name
        it.type = type
    }
}