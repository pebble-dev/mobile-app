package io.rebble.cobble.bluetooth.gatt

import io.rebble.libpebblecommon.util.DataBuffer
import kotlin.math.min

class PendingPacket() {

    private var pendingLength: Int? = null
    private val packetBuf = mutableListOf<Byte>()

    public val data: List<Byte> = packetBuf

    public val isComplete get() = pendingLength == 0

    companion object {
        private fun getPacketLength(packet: List<Byte>): Int {
            val headBuf = DataBuffer(packet.toByteArray().asUByteArray())
            val length = headBuf.getUShort()
            return length.toInt()
        }
    }

    public fun addData(data: List<Byte>): Int {
        if (pendingLength == null) {
            pendingLength = getPacketLength(data)+4
        }
        val toAdd = data.subList(0, min(pendingLength!!-1, data.lastIndex))
        packetBuf.addAll(toAdd)
        pendingLength = pendingLength!! - toAdd.size
        return toAdd.size
    }
}