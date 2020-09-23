package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothDevice
import java.nio.ByteBuffer

class BlueGATTClient : BlueIO {
    override val isConnected get() = TODO("NOT IMPLEMENTED")
    override suspend fun sendPacket(bytes: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun readStream(buffer: ByteBuffer, offset: Int, count: Int): Int {
        TODO("Not yet implemented")
    }

    override fun targetPebble(device: BluetoothDevice): Boolean {
        TODO("Not yet implemented")
    }

    override fun closePebble() {
        TODO("Not yet implemented")
    }

    override fun getTarget(): BluetoothDevice? {
        TODO("Not yet implemented")
    }

    override fun setOnConnectionChange(f: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }
}