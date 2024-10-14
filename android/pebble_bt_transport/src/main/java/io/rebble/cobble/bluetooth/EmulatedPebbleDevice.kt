package io.rebble.cobble.bluetooth

import io.rebble.cobble.shared.domain.common.PebbleDevice
import kotlinx.coroutines.isActive

class EmulatedPebbleDevice(
        address: String
) : PebbleDevice(null, address){

    override fun toString(): String {
        return "< EmulatedPebbleDevice, address=$address, connectionScopeActive=${connectionScope.value?.isActive} >"
    }
}