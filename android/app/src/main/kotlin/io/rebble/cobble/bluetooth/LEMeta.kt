package io.rebble.cobble.bluetooth

import timber.log.Timber
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and

/**
 * Describes the BLE GAP extra data sent by discoverable LE pebbles
 */
@ExperimentalUnsignedTypes
data class LEMeta(private val scanRecord: ByteArray) {
    val vendor: Short
    val payloadType: Byte
    val serialNumber: String

    val hardwarePlatform: UByte?
    val color: Byte?
    val major: UByte?
    val minor: UByte?
    val patch: UByte?

    private val flags: Byte?
    val runningPRF: Boolean?
    val firstUse: Boolean?

    private val mandatoryDataSize = Short.SIZE_BYTES + Byte.SIZE_BYTES + (Char.SIZE_BYTES * 12)
    private val fullDataSize = 32

    init {
        val recordRaw = ByteBuffer.wrap(scanRecord)
        recordRaw.order(ByteOrder.LITTLE_ENDIAN)
        var seekComplete = false
        while (!seekComplete && recordRaw.array().size - recordRaw.position() > 1) {
            val length = recordRaw.get().toUByte()
            val type = recordRaw.get()
            if (type != 0xFF.toByte()) {
                recordRaw.position((recordRaw.position().toUByte() + (length - 1u)).toInt())
            } else {
                seekComplete = true
            }
        }
        if (!seekComplete) {
            Timber.e("No manufacturer specific data in GAP")
            vendor = -1
            payloadType = 0
            serialNumber = "??"

            hardwarePlatform = null
            color = null
            major = null
            minor = null
            patch = null
            flags = null
            runningPRF = null
            firstUse = null
        } else {
            if (recordRaw.array().size - recordRaw.position() >= mandatoryDataSize) {
                vendor = recordRaw.getShort()
                payloadType = recordRaw.get()

                val serialChars = ByteArray(12)
                recordRaw.get(serialChars)
                serialNumber = String(serialChars)
            } else {
                Timber.e("Mandatory manufacturer specific data malformed")
                vendor = -1
                payloadType = 0
                serialNumber = "??"
            }

            var _hardwarePlatform: UByte? = null
            var hasExtras = true

            try {
                _hardwarePlatform = recordRaw.get().toUByte()
            } catch (e: BufferUnderflowException) {
                Timber.w("Extra manufacturer specific data not present")
                hasExtras = false
            }

            if (hasExtras) {
                color = recordRaw.get()
                major = recordRaw.get().toUByte()
                minor = recordRaw.get().toUByte()
                patch = recordRaw.get().toUByte()
                flags = recordRaw.get()

                runningPRF = (flags and 0x01) > 0
                firstUse = (flags and 0x02) > 0
            } else {
                color = null
                major = null
                minor = null
                patch = null
                flags = null
                runningPRF = null
                firstUse = null
            }
            hardwarePlatform = _hardwarePlatform
        }
    }

    override fun toString(): String {
        var result = "<${this::class.java.name} "
        for (prop in this::class.java.declaredFields) {
            result += "${prop.name} = ${prop.get(this)} "
        }
        result += ">"
        return result
    }
}