package io.rebble.cobble.shared.domain.common

import io.ktor.http.parametersOf
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.packets.WatchVersion
import io.rebble.libpebblecommon.services.app.AppRunStateService
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

open class PebbleDevice(
    metadata: WatchVersion.WatchVersionResponse?,
    private val protocolHandler: ProtocolHandler,
    val address: String
): KoinComponent {
    val metadata: MutableStateFlow<WatchVersion.WatchVersionResponse?> = MutableStateFlow(metadata)

    override fun toString(): String = "< PebbleDevice address=$address >"

    //TODO: Move to per-protocol handler services, so we can have multiple PebbleDevices, this is the first of many
    val appRunStateService: AppRunStateService by inject {parametersOf(protocolHandler)}

}