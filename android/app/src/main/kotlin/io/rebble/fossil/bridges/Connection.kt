package io.rebble.fossil.bridges

import io.flutter.plugin.common.BinaryMessenger
import io.rebble.fossil.bluetooth.BlueCommon
import io.rebble.fossil.pigeons.BooleanWrapper
import io.rebble.fossil.pigeons.Pigeons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class Connection @Inject constructor(
        binaryMessenger: BinaryMessenger,
        private val blueCommon: BlueCommon,
        private val coroutineScope: CoroutineScope
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

    @Suppress("UNCHECKED_CAST")
    override fun sendRawPacket(arg: Pigeons.ListWrapper) {
        coroutineScope.launch {
            val byteArray = (arg.value as List<Number>).map { it.toByte() }.toByteArray()
            blueCommon.sendPacket(byteArray)
        }
    }
}