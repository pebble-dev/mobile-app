package io.rebble.fossil.bridges

import io.flutter.plugin.common.BinaryMessenger
import io.rebble.fossil.bluetooth.scan.BleScanner
import io.rebble.fossil.bluetooth.scan.ClassicScanner
import io.rebble.fossil.pigeons.ListWrapper
import io.rebble.fossil.pigeons.Pigeons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalUnsignedTypes::class)
class Scan @Inject constructor(
        binaryMessenger: BinaryMessenger,
        private val bleScanner: BleScanner,
        private val classicScanner: ClassicScanner,
        private val coroutineScope: CoroutineScope
) : FlutterBridge, Pigeons.ScanControl {
    private val scanCallbacks = Pigeons.ScanCallbacks(binaryMessenger)

    init {
        Pigeons.ScanControl.setup(binaryMessenger, this)
    }

    override fun startBleScan() {
        coroutineScope.launch {
            scanCallbacks.onScanStarted { }

            bleScanner.getScanFlow().collect { foundDevices ->
                scanCallbacks.onScanUpdate(ListWrapper(foundDevices.map { it.toPigeon() })) {}
            }

            scanCallbacks.onScanStopped { }
        }
    }

    override fun startClassicScan() {
        coroutineScope.launch {
            scanCallbacks.onScanStarted { }

            classicScanner.getScanFlow().collect { foundDevices ->
                scanCallbacks.onScanUpdate(ListWrapper(foundDevices.map { it.toPigeon() })) {}
            }

            scanCallbacks.onScanStopped { }
        }
    }
}