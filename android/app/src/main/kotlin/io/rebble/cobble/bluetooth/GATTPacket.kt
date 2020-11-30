package io.rebble.cobble.bluetooth

import io.rebble.cobble.util.shl
import io.rebble.cobble.util.shr
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
    val sequence: UShort

    companion object {
        private val typeMask: Byte = 0b111
        private val sequenceMask: Byte = 0b11111000.toByte()
    }

    constructor(data: ByteArray) {
        //Timber.d("${data.toHexString()} -> ${ubyteArrayOf((data[0] and sequenceMask).toUByte()).toHexString()} -> ${ubyteArrayOf((data[0] and sequenceMask).toUByte() shr 3).toHexString()}")
        this.data = data
        sequence = ((data[0] and sequenceMask).toUByte() shr 3).toUShort()
        if (sequence < 0U || sequence > 31U) throw IllegalArgumentException("Sequence must be between 0 and 31 inclusive")
        type = PacketType.fromHeader(data[0])
    }

    constructor(type: PacketType, sequence: UShort, data: ByteArray? = null) {
        this.sequence = sequence
        if (sequence < 0U || sequence > 31U) throw IllegalArgumentException("Sequence must be between 0 and 31 inclusive")
        this.type = type

        if (data != null) {
            this.data = ByteArray(data.size + 1)
        } else {
            this.data = ByteArray(1)
        }

        val dataBuf = ByteBuffer.wrap(this.data)

        dataBuf.put((type.value or (((sequence shl 3) and sequenceMask.toUShort()).toByte())))
        if (data != null) {
            dataBuf.put(data)
        }
    }

    fun toByteArray(): ByteArray {
        return data
    }
}