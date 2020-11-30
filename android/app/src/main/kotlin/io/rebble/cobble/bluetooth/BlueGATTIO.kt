package io.rebble.cobble.bluetooth

import java.io.PipedInputStream

interface BlueGATTIO {
    var isConnected: Boolean
    fun setMTU(newMTU: Int)
    suspend fun requestReset()
    suspend fun connectPebble(): Boolean
    val inputStream: PipedInputStream
}