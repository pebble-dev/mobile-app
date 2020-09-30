package io.rebble.fossil.bridges

import io.flutter.plugin.common.BinaryMessenger
import io.rebble.fossil.bluetooth.BlueCommon
import io.rebble.fossil.bluetooth.ConnectionLooper
import io.rebble.fossil.bluetooth.ConnectionState
import io.rebble.fossil.pigeons.BooleanWrapper
import io.rebble.fossil.pigeons.Pigeons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class Connection @Inject constructor(
        binaryMessenger: BinaryMessenger,
        private val connectionLooper: ConnectionLooper,
        private val blueCommon: BlueCommon,
        private val coroutineScope: CoroutineScope
) : FlutterBridge, Pigeons.ConnectionControl {
    init {
        Pigeons.ConnectionControl.setup(binaryMessenger, this)
    }

    override fun isConnected(): Pigeons.BooleanWrapper {
        return BooleanWrapper(connectionLooper.connectionState.value is ConnectionState.Connected)
    }

    override fun connectToWatch(arg: Pigeons.NumberWrapper) {
        val address = arg.value

        connectionLooper.connectToWatch(address)
    }

    @Suppress("UNCHECKED_CAST")
    override fun sendRawPacket(arg: Pigeons.ListWrapper) {
        coroutineScope.launch {
            val byteArray = (arg.value as List<Number>).map { it.toByte() }.toByteArray()
            blueCommon.sendPacket(byteArray)
        }
    }
}