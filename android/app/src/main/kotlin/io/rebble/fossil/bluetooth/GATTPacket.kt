package io.rebble.fossil.bluetooth

import io.rebble.fossil.util.shl
import io.rebble.fossil.util.shr
import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.or

class GATTPacket {

    enum class PacketType(val value: Byte) {
        DATA(0),
        ACK(1),
        RESET(2),
        RESET_ACK(3);

        companion object {
            fun fromHeader(value: Byte): GATTPacket.PacketType {
                val valueMasked = value and typeMask
                return GATTPacket.PacketType.values().first { it.value == valueMasked }
            }
        }
    }

    val data: ByteArray
    val type: PacketType
    val sequence: Short

    companion object {
        private val typeMask: Byte = 0b111
        private val sequenceMask: Byte = 0b11111000.toByte()
    }

    constructor(data: ByteArray) {
        this.data = data
        sequence = ((data[0] and sequenceMask) shr 3).toShort()
        type = PacketType.fromHeader(data[0])
    }

    constructor(type: PacketType, sequence: Short, data: ByteArray? = null) {
        this.sequence = sequence
        this.type = type

        if (data != null) {
            this.data = ByteArray(data.size + 1)
        } else {
            this.data = ByteArray(1)
        }

        val dataBuf = ByteBuffer.wrap(this.data)

        dataBuf.put((type.value or (((sequence shl 3) and sequenceMask.toShort()).toByte())))
        if (data != null) {
            dataBuf.put(data)
        }
    }

    fun toByteArray(): ByteArray {
        return data
    }
}