package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothDevice
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.libpebblecommon.ProtocolHandler

class EmulatedPebbleDevice(
        address: String,
        protocolHandler: ProtocolHandler
) : PebbleDevice(null, protocolHandler, address){

    override fun toString(): String {
        return "< EmulatedPebbleDevice, address=$address >"
    }
}