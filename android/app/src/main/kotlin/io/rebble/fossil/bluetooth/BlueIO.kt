package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothDevice
import java.nio.ByteBuffer

interface BlueIO {
    val isConnected: Boolean

    suspend fun sendPacket(bytes: ByteArray)
    fun readStream(buffer: ByteBuffer, offset: Int, count: Int): Int
    fun startConnection(): Boolean
    fun closePebble()
    fun getTarget(): BluetoothDevice?

    fun setOnConnectionChange(f: (Boolean) -> Unit)
}