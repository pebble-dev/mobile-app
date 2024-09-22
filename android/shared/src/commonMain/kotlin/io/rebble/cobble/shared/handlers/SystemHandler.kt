package io.rebble.cobble.shared.handlers

import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import io.rebble.libpebblecommon.PacketPriority
import io.rebble.libpebblecommon.packets.PhoneAppVersion
import io.rebble.libpebblecommon.packets.ProtocolCapsFlag
import io.rebble.libpebblecommon.packets.TimeMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalUnsignedTypes::class)
class SystemHandler(
        private val pebbleDevice: PebbleDevice,
) : CobbleHandler, KoinComponent {
    private val negotiationScope = pebbleDevice.negotiationScope
    private val systemService = pebbleDevice.systemService
    private val platformContext: PlatformContext by inject()

    init {
        pebbleDevice.systemService.appVersionRequestHandler = this::handleAppVersionRequest
        listenForTimeChange()

        negotiationScope.launch {
            // Wait until watch is connected before sending time
            ConnectionStateManager.connectionState.first { it is ConnectionState.Connected }

            sendCurrentTime()
        }

        negotiate()
    }

    fun negotiationsComplete(watch: PebbleDevice) {
        if (ConnectionStateManager.connectionState.value is ConnectionState.Negotiating) {
            ConnectionStateManager.connectionState.value = ConnectionState.Connected(watch)
        } else {
            Logging.w("negotiationsComplete state mismatch!")
        }
    }

    fun recoveryMode(watch: PebbleDevice) {
        if (ConnectionStateManager.connectionState.value is ConnectionState.Connected || ConnectionStateManager.connectionState.value is ConnectionState.Negotiating) {
            ConnectionStateManager.connectionState.value = ConnectionState.RecoveryMode(watch)
        } else {
            Logging.w("recoveryMode state mismatch!")
        }
    }

    fun negotiate() {
        negotiationScope.launch {
            ConnectionStateManager.connectionState.first { it is ConnectionState.Negotiating }
            Logging.i("Negotiating with watch")
            refreshWatchMetadata()
            ConnectionStateManager.connectedWatchMetadata.value?.let {
                if (it.running.isRecovery.get()) {
                    Logging.i("Watch is in recovery mode, switching to recovery state")
                    ConnectionStateManager.connectionState.value.watchOrNull?.let { it1 -> recoveryMode(it1) }
                } else {
                    ConnectionStateManager.connectionState.value.watchOrNull?.let { it1 -> negotiationsComplete(it1) }
                }
            }
        }
    }

    private suspend fun refreshWatchMetadata() {
        var retries = 0
        while (retries < 3) {
            try {
                withTimeout(3000) {
                    val watchInfo = systemService.requestWatchVersion()
                    //FIXME: Possible race condition here
                    val watch = ConnectionStateManager.connectionState.value.watchOrNull
                    watch?.metadata?.value = watchInfo
                    val watchModel = systemService.requestWatchModel()
                    watch?.modelId?.value = watchModel
                }
                break
            } catch (e: TimeoutCancellationException) {
                Logging.e("Failed to get watch metadata, retrying", e)
                retries++
            } catch (e: Exception) {
                Logging.e("Failed to get watch metadata", e)
                break
            }
        }
        if (retries >= 3) {
            Logging.e("Failed to get watch metadata after 3 retries, giving up and reconnecting")
            //TODO: double check this works
            negotiationScope.cancel("Failed to get watch metadata")
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun listenForTimeChange() {
        negotiationScope.launch {
            val connectionScope = pebbleDevice.connectionScope.filterNotNull().first()
            platformTimeChangedFlow(platformContext).onEach {
                sendCurrentTime()
            }.launchIn(connectionScope)
        }

    }

    private suspend fun sendCurrentTime() {
        val timezone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()

        val updateTimePacket = TimeMessage.SetUTC(
                now.epochSeconds.toUInt(),
                timezone.offsetAt(now).totalSeconds.seconds.inWholeHours.toShort(),
                timezone.id
        )

        systemService.send(updateTimePacket, PacketPriority.LOW)
    }

    private suspend fun handleAppVersionRequest(): PhoneAppVersion.AppVersionResponse {
       val platformFlags = getPlatformPebbleFlags(platformContext)

        return PhoneAppVersion.AppVersionResponse(
                UInt.MAX_VALUE,
                0u,
                PhoneAppVersion.PlatformFlag.makeFlags(
                        platformContext.osType,
                        platformFlags.toList()
                ),
                2u,
                4u,
                4u,
                2u,
                ProtocolCapsFlag.makeFlags(
                        listOf(
                                ProtocolCapsFlag.Supports8kAppMessage,
                                ProtocolCapsFlag.SupportsExtendedMusicProtocol,
                                ProtocolCapsFlag.SupportsTwoWayDismissal,
                                ProtocolCapsFlag.SupportsAppRunStateProtocol
                        )
                )
        )
    }
}

expect fun platformTimeChangedFlow(context: PlatformContext): Flow<Unit>
expect fun getPlatformPebbleFlags(context: PlatformContext): Set<PhoneAppVersion.PlatformFlag>