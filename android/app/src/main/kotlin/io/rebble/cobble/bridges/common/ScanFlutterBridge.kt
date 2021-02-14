package io.rebble.cobble.bridges.common

import io.rebble.cobble.transport.bluetooth.scan.BleScanner
import io.rebble.cobble.transport.bluetooth.scan.ClassicScanner
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.pigeons.ListWrapper
import io.rebble.cobble.pigeons.Pigeons
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