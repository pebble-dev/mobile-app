package io.rebble.cobble.shared.domain.common

import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.domain.appmessage.AppMessageTransactionSequence
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.timeline.TimelineActionManager
import io.rebble.cobble.shared.domain.voice.VoiceSession
import io.rebble.cobble.shared.handlers.CobbleHandler
import io.rebble.cobble.shared.handlers.OutgoingMessage
import io.rebble.cobble.shared.middleware.PutBytesController
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.packets.AppMessage
import io.rebble.libpebblecommon.packets.WatchVersion
import io.rebble.libpebblecommon.services.*
import io.rebble.libpebblecommon.services.app.AppRunStateService
import io.rebble.libpebblecommon.services.appmessage.AppMessageService
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import io.rebble.libpebblecommon.services.blobdb.TimelineService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

open class PebbleDevice(
    metadata: WatchVersion.WatchVersionResponse?,
    val address: String,
): KoinComponent, AutoCloseable {
    private val negotiationHandlers: Set<CobbleHandler> by inject(named("negotiationDeviceHandlers")) { parametersOf(this as PebbleDevice) }
    private val handlers: Set<CobbleHandler> by inject(named("deviceHandlers")) { parametersOf(this as PebbleDevice) }

    val protocolHandler: ProtocolHandler by inject()
    val negotiationScope = CoroutineScope(Dispatchers.Default + CoroutineName("NegotationScope-$address"))
    val metadata: MutableStateFlow<WatchVersion.WatchVersionResponse?> = MutableStateFlow(metadata)
    val modelId: MutableStateFlow<Int?> = MutableStateFlow(null)
    val connectionScope: MutableStateFlow<CoroutineScope?> = MutableStateFlow(null)
    val currentActiveApp: MutableStateFlow<Uuid?> = MutableStateFlow(null)

    val incomingAppMessages: MutableSharedFlow<AppMessage> = MutableSharedFlow(0, 8)
    val outgoingAppMessages: MutableSharedFlow<OutgoingMessage> = MutableSharedFlow(0, 8)
    val activeVoiceSession: MutableStateFlow<VoiceSession?> = MutableStateFlow(null)

    val appMessageTransactionSequence = AppMessageTransactionSequence().iterator()

    // Required for the system handler
    val systemService: SystemService by inject {parametersOf(protocolHandler)}

    init {
        // This will init all the handlers by reading the lazy value causing them to be injected
        negotiationScope.launch {
            val initNHandlers = negotiationHandlers.joinToString { it::class.simpleName ?: "Unknown" }
            Logging.i("Initialised negotiation handlers: $initNHandlers")
            ConnectionStateManager.connectionState.first { it is ConnectionState.Connected && it.watch.address == address }
            val connectionScope = connectionScope.filterNotNull().first()
            connectionScope.launch {
                val initHandlers = handlers.joinToString { it::class.simpleName ?: "Unknown" }
                Logging.i("Initialised handlers: $initHandlers")
            }
        }
    }

    override fun toString(): String = "< PebbleDevice address=$address >"

    //TODO: Move to per-protocol handler services, so we can have multiple PebbleDevices, this is the first of many
    val appRunStateService: AppRunStateService by inject {parametersOf(protocolHandler)}
    val blobDBService: BlobDBService by inject {parametersOf(protocolHandler)}
    val appMessageService: AppMessageService by inject {parametersOf(protocolHandler)}
    val musicService: MusicService by inject {parametersOf(protocolHandler)}
    val putBytesService: PutBytesService by inject {parametersOf(protocolHandler)}
    val phoneControlService: PhoneControlService by inject {parametersOf(protocolHandler)}
    val appLogsService: AppLogService by inject {parametersOf(protocolHandler)}
    val logDumpService: LogDumpService by inject {parametersOf(protocolHandler)}
    val screenshotService: ScreenshotService by inject {parametersOf(protocolHandler)}
    val timelineService: TimelineService by inject {parametersOf(protocolHandler)}
    val appFetchService: AppFetchService by inject {parametersOf(protocolHandler)}
    val voiceService: VoiceService by inject {parametersOf(protocolHandler)}
    val audioStreamService: AudioStreamService by inject {parametersOf(protocolHandler)}

    val putBytesController: PutBytesController by inject {parametersOf(this)}
    val timelineActionManager: TimelineActionManager by inject {parametersOf(this)}

    override fun close() {
        negotiationScope.cancel("PebbleDevice closed")
        connectionScope.value?.cancel("PebbleDevice closed")
        connectionScope.value = null
    }
}