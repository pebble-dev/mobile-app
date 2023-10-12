package io.rebble.cobble.bridges.common

import android.bluetooth.le.ScanResult
import io.rebble.cobble.BuildConfig
import io.rebble.cobble.bluetooth.BluePebbleDevice
import io.rebble.cobble.bluetooth.scan.BleScanner
import io.rebble.cobble.bluetooth.scan.ClassicScanner
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.pigeons.ListWrapper
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.pigeons.Pigeons.PebbleScanDevicePigeon
import io.rebble.cobble.pigeons.toMapExt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
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
                scanCallbacks.onScanUpdate(ListWrapper(listOf(PebbleScanDevicePigeon().also {
                    it.address = "10.0.2.2" //TODO: make configurable
                    it.name = "Emulator"
                    it.firstUse = false
                    it.runningPRF = false
                    it.serialNumber = "EMULATOR"
                }.toMapExt()))) {}
            }

            bleScanner.getScanFlow().collect { foundDevices ->
                scanCallbacks.onScanUpdate(
                        ListWrapper(foundDevices.map { it.toPigeon().toMapExt() })
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
                        ListWrapper(foundDevices.map { it.toPigeon().toMapExt() })
                ) {}
            }

            scanCallbacks.onScanStopped { }
        }
    }
}