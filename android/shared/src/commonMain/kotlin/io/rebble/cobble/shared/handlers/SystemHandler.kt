package io.rebble.cobble.shared.handlers

import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.libpebblecommon.PacketPriority
import io.rebble.libpebblecommon.packets.PhoneAppVersion
import io.rebble.libpebblecommon.packets.ProtocolCapsFlag
import io.rebble.libpebblecommon.packets.TimeMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
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
            ConnectionStateManager.connectionState.first { it is ConnectionState.Negotiating }
            negotiate()
        }
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
            pebbleDevice.metadata.value?.let {
                if (it.running.isRecovery.get()) {
                    Logging.i("Watch is in recovery mode, switching to recovery state")
                    recoveryMode(pebbleDevice)
                } else {
                    sendCurrentTime()
                    negotiationsComplete(pebbleDevice)
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
                    pebbleDevice.metadata.value = watchInfo
                    val watchModel = systemService.requestWatchModel()
                    pebbleDevice.modelId.value = watchModel
                }
                if (retries > 0) {
                    Logging.i("Successfully got watch metadata after $retries retries")
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
            pebbleDevice.close()
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun listenForTimeChange() {
        negotiationScope.launch {
            val connectionScope = pebbleDevice.connectionScope.filterNotNull().first()
            platformTimeChangedFlow(platformContext).onEach {
                Logging.d("Time/timezone changed, updating time on watch")
                sendCurrentTime()
            }.launchIn(connectionScope)
        }

    }

    private suspend fun sendCurrentTime() {
        val timezone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val timezoneOffsetMinutes = timezone.offsetAt(now).totalSeconds.seconds.inWholeMinutes
        Logging.i("Sending current time to watch: $now, timezone: $timezone, offset: $timezoneOffsetMinutes")
        val updateTimePacket = TimeMessage.SetUTC(
                now.epochSeconds.toUInt(),
                timezoneOffsetMinutes.toShort(),
                timezone.id
        )

        systemService.send(updateTimePacket)
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
                        buildList {
                            add(ProtocolCapsFlag.Supports8kAppMessage)
                            add(ProtocolCapsFlag.SupportsExtendedMusicProtocol)
                            add(ProtocolCapsFlag.SupportsTwoWayDismissal)
                            add(ProtocolCapsFlag.SupportsAppRunStateProtocol)
                            if (platformContext.osType == PhoneAppVersion.OSType.Android) {
                                add(ProtocolCapsFlag.SupportsAppDictation)
                            }
                        }
                )
        )
    }
}

expect fun platformTimeChangedFlow(context: PlatformContext): Flow<Unit>
expect fun getPlatformPebbleFlags(context: PlatformContext): Set<PhoneAppVersion.PlatformFlag>