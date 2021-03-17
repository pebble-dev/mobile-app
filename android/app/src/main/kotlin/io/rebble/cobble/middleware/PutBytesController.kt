package io.rebble.cobble.middleware

import com.squareup.moshi.Moshi
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.data.pbw.manifest.PbwBlob
import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.cobble.util.requirePbwBinaryBlob
import io.rebble.cobble.util.requirePbwManifest
import io.rebble.libpebblecommon.metadata.WatchType
import io.rebble.libpebblecommon.packets.*
import io.rebble.libpebblecommon.services.PutBytesService
import io.rebble.libpebblecommon.util.Crc32Calculator
import io.rebble.libpebblecommon.util.getPutBytesMaximumDataSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okio.BufferedSource
import okio.buffer
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PutBytesController @Inject constructor(
        private val connectionLooper: ConnectionLooper,
        private val putBytesService: PutBytesService,
        private val metadataStore: WatchMetadataStore,
        private val moshi: Moshi
) {
    private val _status: MutableStateFlow<Status> = MutableStateFlow(Status(State.IDLE))
    val status: StateFlow<Status> get() = _status

    private var lastCookie: UInt? = null

    fun startAppInstall(appId: UInt, pbwFile: File, watchType: WatchType) = launchNewPutBytesSession {
        val manifest = requirePbwManifest(moshi, pbwFile, watchType)

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
                    manifest.resources,
                    ObjectType.APP_RESOURCE,
                    progressMultiplier
            )
        }
        if (manifest.worker != null) {
            sendAppPart(
                    appId,
                    pbwFile,
                    watchType,
                    manifest.worker,
                    ObjectType.WORKER,
                    progressMultiplier
            )
        }

        _status.value = Status(State.IDLE)
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
        putBytesService.send(
                PutBytesAppInit(manifestEntry.size.toUInt(), type, appId)
        )

        val source = requirePbwBinaryBlob(pbwFile, watchType, manifestEntry.name)
        val cookie = source.buffer().use {
            awaitCookieAndPutSource(
                    it,
                    manifestEntry.crc,
                    progressMultiplier
            )
        }

        Timber.d("Sending install")

        putBytesService.send(
                PutBytesInstall(cookie)
        )
        awaitAck()

        Timber.d("Install complete")
    }

    private suspend fun awaitCookieAndPutSource(
            source: BufferedSource,
            expectedCrc: Long?,
            progressMultiplier: Double
    ): UInt {
        val cookie = awaitAck().cookie.get()
        lastCookie = cookie

        val maxDataSize = getPutBytesMaximumDataSize(
                metadataStore.lastConnectedWatchMetadata.value
        )

        val buffer = ByteArray(maxDataSize)
        val crcCalculator = Crc32Calculator()

        var totalBytes = 0
        while (true) {
            val readBytes = withContext(Dispatchers.IO) {
                source.read(buffer)
            }

            if (readBytes <= 0) {
                break
            }

            val payload = buffer.copyOf(readBytes).toUByteArray()
            crcCalculator.addBytes(payload)

            putBytesService.send(
                    PutBytesPut(cookie, payload)
            )
            awaitAck()

            val newProgress = status.value.progress + progressMultiplier * readBytes
            Timber.d("Progress %f", newProgress)
            _status.value = Status(State.SENDING, newProgress)
            totalBytes += readBytes
        }

        val calculatedCrc = crcCalculator.finalize()
        if (expectedCrc != null && calculatedCrc != expectedCrc.toUInt()) {
            throw IllegalStateException(
                    "Sending fail: Crc mismatch ($calculatedCrc != $expectedCrc)"
            )
        }

        Timber.d("Sending commit")
        putBytesService.send(
                PutBytesCommit(cookie, calculatedCrc)
        )
        awaitAck()

        return cookie
    }

    private fun launchNewPutBytesSession(block: suspend () -> Unit) {
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

    private suspend fun getResponse(): PutBytesResponse {
        return withTimeout(20_000) {
            val iterator = putBytesService.receivedMessages.iterator()
            if (!iterator.hasNext()) {
                throw IllegalStateException("Received messages channel is closed")
            }

            iterator.next()
        }
    }

    private suspend fun awaitAck(): PutBytesResponse {
        val response = getResponse()

        val result = response.result.get()
        if (result != PutBytesResult.ACK.value) {
            throw IOException("Watch responded with NACK ($result). Aborting transfer")
        }

        return response
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