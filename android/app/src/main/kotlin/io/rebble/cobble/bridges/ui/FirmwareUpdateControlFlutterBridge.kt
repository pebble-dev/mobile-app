package io.rebble.cobble.bridges.ui

import android.net.Uri
import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.watchOrNull
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.cobble.middleware.PutBytesController
import io.rebble.cobble.pigeons.BooleanWrapper
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.pigeons.Pigeons.FirmwareUpdateCallbacks
import io.rebble.cobble.util.launchPigeonResult
import io.rebble.cobble.util.stm32Crc
import io.rebble.cobble.util.zippedSource
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import io.rebble.libpebblecommon.metadata.pbz.manifest.PbzManifest
import io.rebble.libpebblecommon.packets.SystemMessage
import io.rebble.libpebblecommon.services.SystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okio.buffer
import timber.log.Timber
import java.io.File
import java.util.zip.CRC32
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

    override fun beginFirmwareUpdate(fwUri: Pigeons.StringWrapper, result: Pigeons.Result<Pigeons.BooleanWrapper>) {
        coroutineScope.launchPigeonResult(result) {
            val pbzFile = File(Uri.parse(fwUri.value).path!!)
            val manifestFile = pbzFile.zippedSource("manifest.json")
                    ?.buffer()
                    ?: error("manifest.json missing from fw $pbzFile")

            val manifest: PbzManifest = manifestFile.use {
                Json.decodeFromStream(it.inputStream())
            }

            require(manifest.type == "firmware") { "PBZ is not a firmware update" }

            val firmwareBin = pbzFile.zippedSource(manifest.firmware.name)
                    ?.buffer()
                    ?: error("${manifest.firmware.name} missing from fw $pbzFile")
            val systemResources = pbzFile.zippedSource(manifest.resources.name)
                    ?.buffer()
                    ?: error("${manifest.resources.name} missing from app $pbzFile")

            val calculatedFwCRC32 = firmwareBin.use { it.stm32Crc() }
            val calculatedResourcesCRC32 = systemResources.use { it.stm32Crc() }

            check(manifest.firmware.crc == calculatedFwCRC32) {
                "Firmware CRC mismatch: ${manifest.firmware.crc} != $calculatedFwCRC32"
            }

            check(manifest.resources.crc == calculatedResourcesCRC32) {
                "System resources CRC mismatch: ${manifest.resources.crc} != $calculatedResourcesCRC32"
            }

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

            val isCorrectWatchType = manifest.firmware.hwRev == connectedWatchHardware

            if (!isCorrectWatchType) {
                return@launchPigeonResult BooleanWrapper(false)
            }

            val response = systemService.firmwareUpdateStart()
            Timber.d("Firmware update start response: $response")
            firmwareUpdateCallbacks.onFirmwareUpdateStarted {}

            coroutineScope.launch {
                putBytesController.status.collect {
                    firmwareUpdateCallbacks.onFirmwareUpdateProgress(it.progress) {}
                    if (it.state == PutBytesController.State.IDLE) {
                        firmwareUpdateCallbacks.onFirmwareUpdateFinished() {}
                        systemService.send(SystemMessage.FirmwareUpdateComplete())
                        return@collect
                    }
                }
            }
            try {
                putBytesController.startFirmwareInstall(firmwareBin, systemResources, manifest)
            } catch (e: Exception) {
                systemService.send(SystemMessage.FirmwareUpdateFailed())
                throw e
            }
            return@launchPigeonResult BooleanWrapper(true)
        }
    }
}