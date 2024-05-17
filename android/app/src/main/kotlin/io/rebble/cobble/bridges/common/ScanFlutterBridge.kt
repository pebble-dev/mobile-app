package io.rebble.cobble.bridges.common

import io.rebble.cobble.BuildConfig
import io.rebble.cobble.bluetooth.scan.BleScanner
import io.rebble.cobble.bluetooth.scan.ClassicScanner
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.pigeons.Pigeons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalUnsignedTypes::class)
class ScanFlutterBridge @Inject constructor(
        bridgeLifecycleController: BridgeLifecycleController,
        private val bleScanner: BleScanner,
        private val classicScanner: ClassicScanner,
        private val coroutineScope: CoroutineScope
) : FlutterBridge, Pigeons.ScanControl {
    private val scanCallbacks =
            bridgeLifecycleController.createCallbacks(Pigeons::ScanCallbacks)

    init {
        bridgeLifecycleController.setupControl(Pigeons.ScanControl::setup, this)
    }

    override fun startBleScan() {
        coroutineScope.launch {
            scanCallbacks.onScanStarted { }

            if (BuildConfig.DEBUG) {
                scanCallbacks.onScanUpdate(listOf(Pigeons.PebbleScanDevicePigeon.Builder()
                        .setAddress("10.0.2.2")
                        .setName("Emulator")
                        .setFirstUse(false)
                        .setRunningPRF(false)
                        .setSerialNumber("EMULATOR")
                        .build())) {}
            }

            bleScanner.getScanFlow().collect { foundDevices ->
                scanCallbacks.onScanUpdate(
                        foundDevices.map { it.toPigeon() }
                ) {}
            }

            scanCallbacks.onScanStopped { }
        }
    }

    override fun startClassicScan() {
        coroutineScope.launch {
            scanCallbacks.onScanStarted { }

            classicScanner.getScanFlow().collect { foundDevices ->
                scanCallbacks.onScanUpdate(
                        foundDevices.map { it.toPigeon() }
                ) {}
            }

            scanCallbacks.onScanStopped { }
        }
    }
}