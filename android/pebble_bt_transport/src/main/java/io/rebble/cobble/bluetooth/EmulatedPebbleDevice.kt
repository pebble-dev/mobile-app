package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothDevice
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

class EmulatedPebbleDevice(
        address: String
) : PebbleDevice(null, address){

    override fun toString(): String {
        return "< EmulatedPebbleDevice, address=$address, connectionScopeActive=${connectionScope.value?.isActive} >"
    }
}