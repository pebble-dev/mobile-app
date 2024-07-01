package io.rebble.cobble.bluetooth

import io.rebble.cobble.pigeons.Pigeons

@Throws(SecurityException::class)
fun ScannedPebbleDevice.toPigeon(): Pigeons.PebbleScanDevicePigeon {
    return Pigeons.PebbleScanDevicePigeon().also {
        it.name = bluetoothDevice.name
        it.address = bluetoothDevice.address

        if (leMeta?.major != null) {
            it.version = "${leMeta!!.major}.${leMeta!!.minor}.${leMeta!!.patch}"
        }
        if (leMeta?.serialNumber != null) {
            it.serialNumber = leMeta!!.serialNumber
        }
        if (leMeta?.color != null) {
            it.color = leMeta!!.color!!.toLong()
        }
        if (leMeta?.runningPRF != null) {
            it.runningPRF = leMeta!!.runningPRF
        }
        if (leMeta?.firstUse != null) {
            it.firstUse = leMeta!!.firstUse
        }
    }
}