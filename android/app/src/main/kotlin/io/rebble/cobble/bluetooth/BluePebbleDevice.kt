package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.util.macAddressToLong

@OptIn(ExperimentalUnsignedTypes::class)
class BluePebbleDevice {
    val bluetoothDevice: BluetoothDevice
    val leMeta: LEMeta?

    constructor(device: BluetoothDevice) {
        bluetoothDevice = device
        leMeta = null
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(scanResult: ScanResult) {
        bluetoothDevice = scanResult.device
        leMeta = scanResult.scanRecord?.bytes?.let { LEMeta(it) }
    }

    constructor(device: BluetoothDevice, scanRecord: ByteArray) {
        bluetoothDevice = device
        leMeta = LEMeta(scanRecord)
    }

    fun toPigeon(): Pigeons.PebbleScanDevicePigeon {
        return Pigeons.PebbleScanDevicePigeon().also {
            it.name = bluetoothDevice.name
            it.address = bluetoothDevice.address

            if (leMeta?.major != null) {
                it.version = "${leMeta.major}.${leMeta.minor}.${leMeta.patch}"
            }
            if (leMeta?.serialNumber != null) {
                it.serialNumber = leMeta.serialNumber
            }
            if (leMeta?.color != null) {
                it.color = leMeta.color.toLong()
            }
            if (leMeta?.runningPRF != null) {
                it.runningPRF = leMeta.runningPRF
            }
            if (leMeta?.firstUse != null) {
                it.firstUse = leMeta.firstUse
            }
        }
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