package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothDevice
import io.rebble.cobble.shared.domain.common.PebbleDevice

class EmulatedPebbleDevice(
        address: String
) : PebbleDevice(null, address){

    override fun toString(): String {
        return "< EmulatedPebbleDevice, address=$address >"
    }
}