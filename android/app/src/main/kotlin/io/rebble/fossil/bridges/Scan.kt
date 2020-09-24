package io.rebble.fossil.bridges

import io.flutter.plugin.common.BinaryMessenger
import io.rebble.fossil.bluetooth.BlueCommon
import io.rebble.fossil.bluetooth.BluePebbleDevice
import io.rebble.fossil.pigeons.ListWrapper
import io.rebble.fossil.pigeons.Pigeons
import javax.inject.Inject

@OptIn(ExperimentalUnsignedTypes::class)
class Scan @Inject constructor(
        binaryMessenger: BinaryMessenger,
        private val blueCommon: BlueCommon
) : FlutterBridge, Pigeons.ScanControl {
    private val scanCallbacks = Pigeons.ScanCallbacks(binaryMessenger)

    init {
        Pigeons.ScanControl.setup(binaryMessenger, this)
    }

    override fun startScan() {
        val deviceList: MutableList<BluePebbleDevice> = mutableListOf()

        deviceList.clear()
        scanCallbacks.onScanStarted {}

        blueCommon.scanDevicesLE({ el ->
            val oldIn = deviceList.indexOfFirst { p -> p.bluetoothDevice.address == el.bluetoothDevice.address }
            if (oldIn < 0 && el.leMeta?.serialNumber != "??") {
                deviceList.add(el)
                scanCallbacks.onScanUpdate(ListWrapper(deviceList.map { it.toPigeon() })) {}
            }
        })
        {
            scanCallbacks.onScanStopped {}
        }
    }
}