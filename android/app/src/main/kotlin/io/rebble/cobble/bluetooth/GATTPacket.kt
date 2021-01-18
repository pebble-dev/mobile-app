package io.rebble.cobble.bluetooth

import io.rebble.cobble.util.shl
import io.rebble.cobble.util.shr
import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.or

/**
 * Describes a GATT packet, which is NOT a pebble packet, it is simply a discrete chunk of data sent to the watch with a header (the data contents is chunks of the pebble packet currently being sent, size depending on MTU)
 */
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

    enum class PPoGConnectionVersion(val value: Byte, val supportsWindowNegotiation: Boolean, val supportsCoalescedAcking: Boolean) {
        ZERO(0, false, false),
        ONE(1, true, true);

        companion object {
            fun fromByte(value: Byte): PPoGConnectionVersion {
                return PPoGConnectionVersion.values().first { it.value == value }
            }
        }

        override fun toString(): String {
            return "< value = $value, supportsWindowNegotiation = $supportsWindowNegotiation, supportsCoalescedAcking = $supportsCoalescedAcking >"
        }
    }

    val data: ByteArray
    val type: PacketType
    val sequence: Int

    companion object {
        private const val typeMask: Byte = 0b111
        private const val sequenceMask: Byte = 0b11111000.toByte()
    }

    constructor(data: ByteArray) {
        //Timber.d("${data.toHexString()} -> ${ubyteArrayOf((data[0] and sequenceMask).toUByte()).toHexString()} -> ${ubyteArrayOf((data[0] and sequenceMask).toUByte() shr 3).toHexString()}")
        this.data = data
        sequence = ((data[0] and sequenceMask).toUByte() shr 3).toInt()
        if (sequence < 0 || sequence > 31) throw IllegalArgumentException("Sequence must be between 0 and 31 inclusive")
        type = PacketType.fromHeader(data[0])
    }

    constructor(type: PacketType, sequence: Int, data: ByteArray? = null) {
        this.sequence = sequence
        if (sequence < 0 || sequence > 31) throw IllegalArgumentException("Sequence must be between 0 and 31 inclusive")
        this.type = type

        if (data != null) {
            this.data = ByteArray(data.size + 1)
        } else {
            this.data = ByteArray(1)
        }

        val dataBuf = ByteBuffer.wrap(this.data)

        dataBuf.put((type.value or (((sequence shl 3) and sequenceMask.toInt()).toByte())))
        if (data != null) {
            dataBuf.put(data)
        }
    }

    fun toByteArray(): ByteArray {
        return data
    }

    fun getPPoGConnectionVersion(): PPoGConnectionVersion {
        if (type != PacketType.RESET) throw IllegalStateException("Function does not apply to packet type")
        return PPoGConnectionVersion.fromByte(data[1])
    }

    fun getMaxTXWindow(): Byte {
        if (type != PacketType.RESET_ACK) throw IllegalStateException("Function does not apply to packet type")
        return data[2]
    }

    fun getMaxRXWindow(): Byte {
        if (type != PacketType.RESET_ACK) throw IllegalStateException("Function does not apply to packet type")
        return data[1]
    }
}