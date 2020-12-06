package io.rebble.cobble.datasources

import io.rebble.libpebblecommon.packets.WatchVersion
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchMetadataStore @Inject constructor() {
    val lastConnectedWatchMetadata = MutableStateFlow<WatchVersion.WatchVersionResponse?>(null)
    val lastConnectedWatchModel = MutableStateFlow<Int?>(null)
}