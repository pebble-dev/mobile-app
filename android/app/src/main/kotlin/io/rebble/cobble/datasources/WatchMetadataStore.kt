package io.rebble.cobble.datasources

import io.rebble.libpebblecommon.packets.WatchVersion
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

//TODO: Consolidate this with the shared ConnectionStateManager
@Singleton
class WatchMetadataStore @Inject constructor() {
    val lastConnectedWatchMetadata = MutableStateFlow<WatchVersion.WatchVersionResponse?>(null)
    val lastConnectedWatchModel = MutableStateFlow<Int?>(null)
    val currentActiveApp = MutableStateFlow<UUID?>(null)
}