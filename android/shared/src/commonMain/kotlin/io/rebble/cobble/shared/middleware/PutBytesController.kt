package io.rebble.cobble.shared.middleware

import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import io.rebble.cobble.shared.util.File
import io.rebble.cobble.shared.util.requirePbwBinaryBlob
import io.rebble.cobble.shared.util.requirePbwManifest
import io.rebble.libpebblecommon.metadata.WatchType
import io.rebble.libpebblecommon.metadata.pbw.manifest.PbwBlob
import io.rebble.libpebblecommon.metadata.pbz.manifest.PbzManifest
import io.rebble.libpebblecommon.packets.ObjectType
import io.rebble.libpebblecommon.packets.PutBytesAbort
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.buffer
import okio.use
import org.koin.core.component.KoinComponent

class PutBytesController(pebbleDevice: PebbleDevice) : KoinComponent {
    private val putBytesService = pebbleDevice.putBytesService
    private val _status: MutableStateFlow<Status> = MutableStateFlow(Status(State.IDLE))
    private val statusMutex = Mutex()
    val status: StateFlow<Status> get() = _status

    private var lastCookie: UInt? = null

    var lastProgress = 0.0
        private set

    fun startAppInstall(
        appId: UInt,
        pbwFile: File,
        watchType: WatchType
    ) = launchNewPutBytesSession {
        val manifest = requirePbwManifest(pbwFile, watchType)

        val totalSize =
            manifest.application.size +
                (manifest.resources?.size ?: 0) +
                (manifest.worker?.size ?: 0)

        val progressMultiplier = 1 / totalSize.toDouble()

        sendAppPart(
            appId,
            pbwFile,
            watchType,
            manifest.application,
            ObjectType.APP_EXECUTABLE,
            progressMultiplier
        )

        if (manifest.resources != null) {
            sendAppPart(
                appId,
                pbwFile,
                watchType,
                manifest.resources!!,
                ObjectType.APP_RESOURCE,
                progressMultiplier
            )
        }
        if (manifest.worker != null) {
            sendAppPart(
                appId,
                pbwFile,
                watchType,
                manifest.worker!!,
                ObjectType.WORKER,
                progressMultiplier
            )
        }

        _status.value = Status(State.IDLE)
    }

    fun startFirmwareInstall(
        firmware: ByteArray,
        resources: ByteArray?,
        manifest: PbzManifest
    ) = launchNewPutBytesSession {
        lastProgress = 0.0
        val totalSize = manifest.firmware.size + (manifest.resources?.size ?: 0)
        require(manifest.firmware.type == "normal" || resources == null) {
            "Resources are only supported for normal firmware"
        }
        var count = 0
        val progressJob =
            launch {
                try {
                    while (isActive) {
                        val progress = putBytesService.progressUpdates.receive()
                        count += progress.delta
                        val nwProgress = count / totalSize.toDouble()
                        lastProgress = nwProgress
                        _status.value = Status(State.SENDING, nwProgress)
                    }
                } catch (_: CancellationException) {
                }
            }
        try {
            resources?.let {
                putBytesService.sendFirmwarePart(
                    it,
                    ConnectionStateManager.connectionState.value.watchOrNull?.metadata?.value!!,
                    manifest.resources!!.crc,
                    manifest.resources!!.size.toUInt(),
                    0u,
                    ObjectType.SYSTEM_RESOURCE
                )
            }
            putBytesService.sendFirmwarePart(
                firmware,
                ConnectionStateManager.connectionState.value.watchOrNull?.metadata?.value!!,
                manifest.firmware.crc,
                manifest.firmware.size.toUInt(),
                when {
                    manifest.resources != null -> 2u
                    else -> 1u
                },
                when (manifest.firmware.type) {
                    "normal" -> ObjectType.FIRMWARE
                    "recovery" -> ObjectType.RECOVERY
                    else -> throw IllegalArgumentException(
                        "Unknown firmware type ${manifest.firmware.type}"
                    )
                }
            )
        } finally {
            progressJob.cancel()
            _status.value = Status(State.IDLE)
        }
    }

    private suspend fun sendAppPart(
        appId: UInt,
        pbwFile: File,
        watchType: WatchType,
        manifestEntry: PbwBlob,
        type: ObjectType,
        progressMultiplier: Double
    ) {
        Logging.d(
            "Send app part $watchType, $appId, $manifestEntry, $type, ${type.value}, $progressMultiplier"
        )
        val source = requirePbwBinaryBlob(pbwFile, watchType, manifestEntry.name)
        source.buffer().use {
            putBytesService.sendAppPart(
                appId,
                it.readByteArray(),
                watchType,
                ConnectionStateManager.connectionState.value.watchOrNull?.metadata?.value!!,
                manifestEntry,
                type
            )
        }
    }

    private fun launchNewPutBytesSession(block: suspend CoroutineScope.() -> Unit): Job {
        return ConnectionStateManager.connectionState.value.watchOrNull?.connectionScope?.value!!.launch {
            statusMutex.withLock {
                if (_status.value.state != State.IDLE) {
                    throw IllegalStateException("Put bytes operation already in progress")
                }

                _status.value = Status(State.SENDING)
            }
            try {
                block()
            } catch (e: Exception) {
                val cookie = lastCookie
                Logging.e("PutBytes error", e)

                if (cookie != null) {
                    putBytesService.send(PutBytesAbort(cookie))
                }
            } finally {
                lastCookie = null
                _status.value = Status(State.IDLE)
            }
        }
    }

    data class Status(
        val state: State,
        val progress: Double = 0.0
    )

    enum class State {
        IDLE,
        SENDING
    }
}