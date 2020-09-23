package io.rebble.fossil.bridges

import io.flutter.plugin.common.BinaryMessenger
import io.rebble.fossil.bluetooth.BlueCommon
import io.rebble.fossil.bluetooth.BooleanWrapper
import io.rebble.fossil.pigeons.Pigeons
import javax.inject.Inject

class Connection @Inject constructor(
        binaryMessenger: BinaryMessenger,
        private val blueCommon: BlueCommon
) : FlutterBridge, Pigeons.ConnectionControl {
    init {
        Pigeons.ConnectionControl.setup(binaryMessenger, this)
    }

    override fun isConnected(): Pigeons.BooleanWrapper {
        return BooleanWrapper(blueCommon.driver?.isConnected == true)
    }

    override fun connectToWatch(arg: Pigeons.NumberWrapper) {
        val address = arg.value

        blueCommon.targetPebble(address)
    }

}