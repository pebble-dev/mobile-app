package io.rebble.cobble.shared.domain.common

import com.benasher44.uuid.Uuid
import io.ktor.http.parametersOf
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.packets.WatchVersion
import io.rebble.libpebblecommon.services.MusicService
import io.rebble.libpebblecommon.services.SystemService
import io.rebble.libpebblecommon.services.app.AppRunStateService
import io.rebble.libpebblecommon.services.appmessage.AppMessageService
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

open class PebbleDevice(
    metadata: WatchVersion.WatchVersionResponse?,
    private val protocolHandler: ProtocolHandler,
    val address: String,
): KoinComponent, AutoCloseable {
    val negotiationScope = CoroutineScope(Dispatchers.Default + CoroutineName("NegotationScope-$address"))
    val metadata: MutableStateFlow<WatchVersion.WatchVersionResponse?> = MutableStateFlow(metadata)
    val modelId: MutableStateFlow<Int?> = MutableStateFlow(null)
    val connectionScope: MutableStateFlow<CoroutineScope?> = MutableStateFlow(null)
    val currentActiveApp: MutableStateFlow<Uuid?> = MutableStateFlow(null)

    override fun toString(): String = "< PebbleDevice address=$address >"

    //TODO: Move to per-protocol handler services, so we can have multiple PebbleDevices, this is the first of many
    val appRunStateService: AppRunStateService by inject {parametersOf(protocolHandler)}
    val blobDBService: BlobDBService by inject {parametersOf(protocolHandler)}
    val appMessageService: AppMessageService by inject {parametersOf(protocolHandler)}
    val systemService: SystemService by inject {parametersOf(protocolHandler)}
    val musicService: MusicService by inject {parametersOf(protocolHandler)}

    override fun close() {
        negotiationScope.cancel("PebbleDevice closed")
    }
}