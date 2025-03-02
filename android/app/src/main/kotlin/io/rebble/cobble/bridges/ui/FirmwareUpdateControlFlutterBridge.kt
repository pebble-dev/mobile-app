package io.rebble.cobble.bridges.ui

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.pigeons.BooleanWrapper
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import io.rebble.cobble.shared.handlers.SystemHandler
import io.rebble.cobble.shared.util.zippedSource
import io.rebble.cobble.util.launchPigeonResult
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import io.rebble.libpebblecommon.metadata.pbz.manifest.PbzManifest
import io.rebble.libpebblecommon.packets.SystemMessage
import io.rebble.libpebblecommon.packets.TimeMessage
import io.rebble.libpebblecommon.util.Crc32Calculator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okio.buffer
import timber.log.Timber
import java.io.InputStream
import java.util.TimeZone
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class FirmwareUpdateControlFlutterBridge
    @Inject
    constructor(
        bridgeLifecycleController: BridgeLifecycleController,
        private val coroutineScope: CoroutineScope,
        private val context: Context
    ) : FlutterBridge, Pigeons.FirmwareUpdateControl {
        init {
            bridgeLifecycleController.setupControl(Pigeons.FirmwareUpdateControl::setup, this)
        }

        private val firmwareUpdateCallbacks =
            bridgeLifecycleController.createCallbacks(
                Pigeons::FirmwareUpdateCallbacks
            )

        override fun checkFirmwareCompatible(
            fwUri: Pigeons.StringWrapper,
            result: Pigeons.Result<Pigeons.BooleanWrapper>
        ) {
            coroutineScope.launchPigeonResult(result) {
                val uri = Uri.parse(fwUri.value)
                val pbzStream =
                    if (uri.scheme == "file" || uri.scheme == "content") {
                        context.applicationContext.contentResolver.openInputStream(uri)
                    } else {
                        uri.toFile().inputStream()
                    } ?: error("Failed to open input stream for $uri")
                val manifest =
                    pbzStream.use {
                        val manifestFile =
                            pbzStream.zippedSource("manifest.json")
                                ?.buffer()
                                ?: error("manifest.json missing from app $uri")
                        val manifest: PbzManifest =
                            manifestFile.use {
                                Json.decodeFromStream(it.inputStream())
                            }
                        require(manifest.type == "firmware") { "PBZ is not a firmware update" }
                        return@use manifest
                    }

                val hardwarePlatformNumber =
                    withTimeoutOrNull(2_000) {
                        ConnectionStateManager.connectionState.first {
                            it.watchOrNull?.metadata?.value != null
                        }.watchOrNull?.metadata?.value
                    }
                        ?.running
                        ?.hardwarePlatform
                        ?.get()
                        ?: error("Watch not connected")

                val connectedWatchHardware =
                    WatchHardwarePlatform
                        .fromProtocolNumber(hardwarePlatformNumber)
                        ?: error("Unknown hardware platform $hardwarePlatformNumber")

                return@launchPigeonResult BooleanWrapper(
                    manifest.firmware.hwRev == connectedWatchHardware
                )
            }
        }

        private fun openZippedFile(
            stream: InputStream,
            path: String
        ) = stream.zippedSource(path)
            ?.buffer()
            ?: error("$path missing from $stream")

        private suspend fun sendTime() {
            val timezone = TimeZone.getDefault()
            val now = System.currentTimeMillis().milliseconds.inWholeSeconds
            val offsetMinutes = timezone.getOffset(now).milliseconds.inWholeMinutes

            val updateTimePacket =
                TimeMessage.SetUTC(
                    now.toUInt(),
                    offsetMinutes.toShort(),
                    timezone.id.take(SystemHandler.MAX_TIMEZONE_NAME_LENGTH)
                )

            ConnectionStateManager.connectionState.value.watchOrNull?.systemService?.send(
                updateTimePacket
            )
        }

        override fun beginFirmwareUpdate(
            fwUri: Pigeons.StringWrapper,
            result: Pigeons.Result<Pigeons.BooleanWrapper>
        ) {
            coroutineScope.launchPigeonResult(result) {
                Timber.d("Begin firmware update")
                val uri = Uri.parse(fwUri.value)

                val manifestFile =
                    openZippedFile(
                        context.applicationContext.contentResolver.openInputStream(uri) ?: error("Couldn't open stream"),
                        "manifest.json"
                    )

                val manifest: PbzManifest =
                    manifestFile.use {
                        Json.decodeFromStream(it.inputStream())
                    }

                require(manifest.type == "firmware") { "PBZ is not a firmware update" }

                val firmwareBin =
                    openZippedFile(
                        context.applicationContext.contentResolver.openInputStream(uri) ?: error("Couldn't open stream"),
                        manifest.firmware.name
                    ).use {
                        it.readByteArray()
                    }
                val systemResources =
                    manifest.resources?.let {
                            res ->
                        openZippedFile(
                            context.applicationContext.contentResolver.openInputStream(uri) ?: error("Couldn't open stream"),
                            res.name
                        ).use {
                            it.readByteArray()
                        }
                    }

                val calculatedFwCRC32 =
                    Crc32Calculator().apply {
                        addBytes(firmwareBin.asUByteArray())
                    }.finalize().toLong()
                val calculatedResourcesCRC32 =
                    systemResources?.let { res ->
                        Crc32Calculator().apply {
                            addBytes(res.asUByteArray())
                        }.finalize().toLong()
                    }

                check(manifest.firmware.crc == calculatedFwCRC32) {
                    "Firmware CRC mismatch: ${manifest.firmware.crc} != $calculatedFwCRC32"
                }

                check(manifest.resources?.crc == calculatedResourcesCRC32) {
                    "System resources CRC mismatch: ${manifest.resources?.crc} != $calculatedResourcesCRC32"
                }

                val lastConnectedWatch =
                    withTimeoutOrNull(2_000) {
                        ConnectionStateManager.connectionState.first {
                            it.watchOrNull?.metadata?.value != null
                        }.watchOrNull?.metadata?.value
                    }
                        ?: error("Watch not connected")

                val hardwarePlatformNumber = lastConnectedWatch.running.hardwarePlatform.get()

                val connectedWatchHardware =
                    WatchHardwarePlatform
                        .fromProtocolNumber(hardwarePlatformNumber)
                        ?: error("Unknown hardware platform $hardwarePlatformNumber")

                val isCorrectWatchType = manifest.firmware.hwRev == connectedWatchHardware

                if (!isCorrectWatchType) {
                    Timber.e(
                        "Firmware update not compatible with connected watch: ${manifest.firmware.hwRev} != $connectedWatchHardware"
                    )
                    return@launchPigeonResult BooleanWrapper(false)
                }

                Timber.i("All checks passed, starting firmware update")
                sendTime()
                val updatingDevice = ConnectionStateManager.connectionState.value.watchOrNull
                val connectionScope = updatingDevice?.connectionScope?.value ?: error("Watch not connected")
                val response =
                    updatingDevice.systemService.firmwareUpdateStart(
                        0u,
                        (
                            manifest.firmware.size + (
                                manifest.resources?.size
                                    ?: 0
                            )
                        ).toUInt()
                    )
                Timber.d("Firmware update start response: $response")
                firmwareUpdateCallbacks.onFirmwareUpdateStarted {}
                val job =
                    connectionScope.launch {
                        try {
                            updatingDevice.putBytesController.status.collect {
                                withContext(Dispatchers.Main) {
                                    firmwareUpdateCallbacks.onFirmwareUpdateProgress(it.progress) {}
                                }
                            }
                        } catch (_: CancellationException) {
                        }
                    }
                try {
                    updatingDevice.putBytesController.startFirmwareInstall(
                        firmwareBin,
                        systemResources,
                        manifest
                    ).join()
                } finally {
                    job.cancel()
                    if (updatingDevice.putBytesController.lastProgress != 1.0) {
                        updatingDevice.systemService.send(SystemMessage.FirmwareUpdateFailed())
                        error(
                            "Firmware update failed - Only reached ${updatingDevice.putBytesController.status.value.progress}"
                        )
                    } else {
                        updatingDevice.systemService.send(SystemMessage.FirmwareUpdateComplete())
                        firmwareUpdateCallbacks.onFirmwareUpdateFinished {}
                    }
                }
                return@launchPigeonResult BooleanWrapper(true)
            }
        }
    }