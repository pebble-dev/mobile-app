package io.rebble.cobble.middleware

import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.cobble.util.requirePbwBinaryBlob
import io.rebble.cobble.util.requirePbwManifest
import io.rebble.libpebblecommon.metadata.WatchType
import io.rebble.libpebblecommon.metadata.pbw.manifest.PbwBlob
import io.rebble.libpebblecommon.metadata.pbz.manifest.PbzManifest
import io.rebble.libpebblecommon.packets.*
import io.rebble.libpebblecommon.services.PutBytesService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okio.BufferedSource
import okio.buffer
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PutBytesController @Inject constructor(
        private val connectionLooper: ConnectionLooper,
        private val putBytesService: PutBytesService,
        private val metadataStore: WatchMetadataStore
) {
    private val _status: MutableStateFlow<Status> = MutableStateFlow(Status(State.IDLE))
    val status: StateFlow<Status> get() = _status

    private var lastCookie: UInt? = null

    fun startAppInstall(appId: UInt, pbwFile: File, watchType: WatchType) = launchNewPutBytesSession {
        val manifest = requirePbwManifest(pbwFile, watchType)

        val totalSize = manifest.application.size +
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

    fun startFirmwareInstall(firmware: BufferedSource, resources: BufferedSource, manifest: PbzManifest) = launchNewPutBytesSession {

        val totalSize = manifest.firmware.size + manifest.resources.size

        val progressMultiplier = 1 / totalSize.toDouble()
        val progressJob = launch{
            try {
                for (progress: PutBytesService.PutBytesProgress in putBytesService.progressUpdates) {
                    _status.value = Status(State.SENDING, progress.count * progressMultiplier)
                }
            } catch (_: CancellationException) {}
        }
        try {
            firmware.use {
                putBytesService.sendFirmwarePart(
                        it.readByteArray(),
                        metadataStore.lastConnectedWatchMetadata.value!!,
                        manifest.firmware.crc,
                        manifest.firmware.size.toUInt(),
                        when (manifest.firmware.type) {
                            "firmware" -> ObjectType.FIRMWARE
                            "recovery" -> ObjectType.RECOVERY
                            else -> throw IllegalArgumentException("Unknown firmware type")
                        },
                        manifest.firmware.name
                )
            }
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
        Timber.d("Send app part %s %s %s %s %s %f",
                watchType, appId, manifestEntry, type, type.value, progressMultiplier)
        val source = requirePbwBinaryBlob(pbwFile, watchType, manifestEntry.name)
        source.buffer().use {
            putBytesService.sendAppPart(
                    appId,
                    it.readByteArray(),
                    watchType,
                    metadataStore.lastConnectedWatchMetadata.value!!,
                    manifestEntry,
                    type
            )
        }
    }

    private fun launchNewPutBytesSession(block: suspend CoroutineScope.() -> Unit) {
        synchronized(_status) {
            if (_status.value.state != State.IDLE) {
                throw IllegalStateException("Put bytes operation already in progress")
            }

            _status.value = Status(State.SENDING)
        }

        connectionLooper.getWatchConnectedScope().launch {
            try {
                block()
            } catch (e: Exception) {
                val cookie = lastCookie
                Timber.e(e, "PutBytes error")

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