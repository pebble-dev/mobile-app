package io.rebble.cobble.bluetooth.gatt

import io.rebble.libpebblecommon.util.DataBuffer
import kotlin.math.min

class PendingPacket() {

    private var pendingLength: Int? = null
    private val packetBuf = mutableListOf<Byte>()

    val data: List<Byte> = packetBuf

    val isComplete get() = pendingLength == 0

    companion object {
        private fun getPacketLength(packet: List<Byte>): Int {
            val headBuf = DataBuffer(packet.toByteArray().asUByteArray())
            return headBuf.getUShort().toInt()
        }
    }

    fun addData(data: List<Byte>): Int {
        if (pendingLength == null) {
            require(data.size >= 4) {"First packet must have a size greater than 4 (header)"}
            pendingLength = getPacketLength(data)+4
        }
        val toAdd = data.subList(0, min(pendingLength!!, data.size))
        packetBuf.addAll(toAdd)
        pendingLength = pendingLength!! - toAdd.size
        return toAdd.size
    }
}