package io.rebble.cobble.bridges.ui

import android.net.Uri
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.cobble.middleware.PutBytesController
import io.rebble.cobble.pigeons.BooleanWrapper
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.util.launchPigeonResult
import io.rebble.cobble.util.zippedSource
import io.rebble.libpebblecommon.PacketPriority
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import io.rebble.libpebblecommon.metadata.pbz.manifest.PbzManifest
import io.rebble.libpebblecommon.packets.SystemMessage
import io.rebble.libpebblecommon.packets.TimeMessage
import io.rebble.libpebblecommon.services.SystemService
import io.rebble.libpebblecommon.util.Crc32Calculator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okio.BufferedSource
import okio.buffer
import timber.log.Timber
import java.io.File
import java.util.TimeZone
import javax.inject.Inject

class FirmwareUpdateControlFlutterBridge @Inject constructor(
        bridgeLifecycleController: BridgeLifecycleController,
        private val coroutineScope: CoroutineScope,
        private val watchMetadataStore: WatchMetadataStore,
        private val systemService: SystemService,
        private val putBytesController: PutBytesController,
) : FlutterBridge, Pigeons.FirmwareUpdateControl {
    init {
        bridgeLifecycleController.setupControl(Pigeons.FirmwareUpdateControl::setup, this)
    }

    private val firmwareUpdateCallbacks = bridgeLifecycleController.createCallbacks(Pigeons::FirmwareUpdateCallbacks)

    override fun checkFirmwareCompatible(fwUri: Pigeons.StringWrapper, result: Pigeons.Result<Pigeons.BooleanWrapper>) {
        coroutineScope.launchPigeonResult(result) {
            val pbzFile = File(Uri.parse(fwUri.value).path!!)
            val manifestFile = pbzFile.zippedSource("manifest.json")
                    ?.buffer()
                    ?: error("manifest.json missing from app $pbzFile")

            val manifest: PbzManifest = manifestFile.use {
                Json.decodeFromStream(it.inputStream())
            }
            require(manifest.type == "firmware") { "PBZ is not a firmware update" }

            val hardwarePlatformNumber = withTimeoutOrNull(2_000) {
                watchMetadataStore.lastConnectedWatchMetadata.first { it != null }
            }
                    ?.running
                    ?.hardwarePlatform
                    ?.get()
                    ?: error("Watch not connected")

            val connectedWatchHardware = WatchHardwarePlatform
                    .fromProtocolNumber(hardwarePlatformNumber)
                    ?: error("Unknown hardware platform $hardwarePlatformNumber")

            return@launchPigeonResult BooleanWrapper(manifest.firmware.hwRev == connectedWatchHardware)
        }
    }

    private fun openZippedFile(file: File, path: String) = file.zippedSource(path)
            ?.buffer()
            ?: error("$path missing from $file")

    private suspend fun sendTime() {
        val timezone = TimeZone.getDefault()
        val now = System.currentTimeMillis()

        val updateTimePacket = TimeMessage.SetUTC(
                (now / 1000).toUInt(),
                timezone.getOffset(now).toShort(),
                timezone.id
        )

        systemService.send(updateTimePacket)
    }

    override fun beginFirmwareUpdate(fwUri: Pigeons.StringWrapper, result: Pigeons.Result<Pigeons.BooleanWrapper>) {
        coroutineScope.launchPigeonResult(result) {
            Timber.d("Begin firmware update")
            val pbzFile = File(Uri.parse(fwUri.value).path!!)
            val manifestFile = openZippedFile(pbzFile, "manifest.json")

            val manifest: PbzManifest = manifestFile.use {
                Json.decodeFromStream(it.inputStream())
            }

            require(manifest.type == "firmware") { "PBZ is not a firmware update" }

            val firmwareBin = openZippedFile(pbzFile, manifest.firmware.name).use { it.readByteArray() }
            val systemResources = manifest.resources?.let {res -> openZippedFile(pbzFile, res.name).use { it.readByteArray() } }

            val calculatedFwCRC32 = Crc32Calculator().apply {
                addBytes(firmwareBin.asUByteArray())
            }.finalize().toLong()
            val calculatedResourcesCRC32 = systemResources?.let {res ->
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

            val lastConnectedWatch = withTimeoutOrNull(2_000) {
                watchMetadataStore.lastConnectedWatchMetadata.first { it != null }
            }
                    ?: error("Watch not connected")

            val hardwarePlatformNumber = lastConnectedWatch.running.hardwarePlatform.get()

            val connectedWatchHardware = WatchHardwarePlatform
                    .fromProtocolNumber(hardwarePlatformNumber)
                    ?: error("Unknown hardware platform $hardwarePlatformNumber")

            val isCorrectWatchType = manifest.firmware.hwRev == connectedWatchHardware

            if (!isCorrectWatchType) {
                Timber.e("Firmware update not compatible with connected watch: ${manifest.firmware.hwRev} != $connectedWatchHardware")
                return@launchPigeonResult BooleanWrapper(false)
            }

            Timber.i("All checks passed, starting firmware update")
            sendTime()
            val response = systemService.firmwareUpdateStart(0u, (manifest.firmware.size + (manifest.resources?.size ?: 0)).toUInt())
            Timber.d("Firmware update start response: $response")
            firmwareUpdateCallbacks.onFirmwareUpdateStarted {}
            val job = coroutineScope.launch {
                try {
                    putBytesController.status.collect {
                        firmwareUpdateCallbacks.onFirmwareUpdateProgress(it.progress) {}
                    }
                } catch (_: CancellationException) { }
            }
            try {
                putBytesController.startFirmwareInstall(firmwareBin, systemResources, manifest).join()
            } finally {
                job.cancel()
                if (putBytesController.lastProgress != 1.0) {
                    systemService.send(SystemMessage.FirmwareUpdateFailed())
                    error("Firmware update failed - Only reached ${putBytesController.status.value.progress}")
                } else {
                    systemService.send(SystemMessage.FirmwareUpdateComplete())
                    firmwareUpdateCallbacks.onFirmwareUpdateFinished() {}
                }
            }
            return@launchPigeonResult BooleanWrapper(true)
        }
    }
}