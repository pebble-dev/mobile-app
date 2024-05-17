package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi

@OptIn(ExperimentalUnsignedTypes::class)
class BluePebbleDevice {
    val bluetoothDevice: BluetoothDevice
    val leMeta: LEMeta?

    constructor(device: BluetoothDevice) {
        bluetoothDevice = device
        leMeta = null
    }

    constructor(scanResult: ScanResult) {
        bluetoothDevice = scanResult.device
        leMeta = scanResult.scanRecord?.bytes?.let { LEMeta(it) }
    }

    constructor(device: BluetoothDevice, scanRecord: ByteArray) {
        bluetoothDevice = device
        leMeta = LEMeta(scanRecord)
    }

    override fun toString(): String {
        var result = "<${this::class.java.name} "
        for (prop in this::class.java.declaredFields) {
            result += "${prop.name} = ${prop.get(this)} "
        }
        result += ">"
        return result
    }
}