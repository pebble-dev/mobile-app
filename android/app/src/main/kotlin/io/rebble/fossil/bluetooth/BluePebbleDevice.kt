package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.collection.ArrayMap

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

    fun toPigeon(): Map<String, Any> {
        val map = ArrayMap<String, Any>()

        map["name"] = bluetoothDevice.name
        map["address"] = bluetoothDevice.address.replace(":", "").toLong(16)

        if (leMeta?.major != null) {
            map["version"] = "${leMeta.major}.${leMeta.minor}.${leMeta.patch}"
        }
        if (leMeta?.serialNumber != null) {
            map["serialNumber"] = leMeta.serialNumber
        }
        if (leMeta?.color != null) {
            map["color"] = leMeta.color
        }
        if (leMeta?.runningPRF != null) {
            map["runningPRF"] = leMeta.runningPRF
        }
        if (leMeta?.firstUse != null) {
            map["firstUse"] = leMeta.firstUse
        }

        return map
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