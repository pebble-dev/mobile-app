package io.rebble.cobble.bluetooth.ble

import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import io.rebble.libpebblecommon.structmapper.SUShort
import io.rebble.libpebblecommon.structmapper.StructMapper
import io.rebble.libpebblecommon.util.DataBuffer
import kotlinx.coroutines.flow.flow
import java.nio.ByteBuffer
import kotlin.math.min

class PPoGPebblePacketAssembler {
    private var data: ByteBuffer? = null

    /**
     * Emits one or more [PebblePacket]s if the data is complete.
     */
    fun assemble(dataToAdd: ByteArray) = flow {
        val dataToAddBuf = ByteBuffer.wrap(dataToAdd)
        while (dataToAddBuf.hasRemaining()) {
            if (data == null) {
                if (dataToAddBuf.remaining() < 4) {
                    throw PPoGPebblePacketAssemblyException("Not enough data for header")
                }
                beginAssembly(dataToAddBuf.slice())
                dataToAddBuf.position(dataToAddBuf.position() + 4)
            }

            val remaining = data!!.remaining()
            val toRead = min(remaining, dataToAddBuf.remaining())
            data!!.put(dataToAddBuf.array(), dataToAddBuf.position(), toRead)
            dataToAddBuf.position(dataToAddBuf.position() + toRead)

            if (data!!.remaining() == 0) {
                data!!.flip()
                val packet = PebblePacket.deserialize(data!!.array().toUByteArray())
                emit(packet)
                clear()
            }
        }
    }

    private fun beginAssembly(headerSlice: ByteBuffer) {
        val meta = StructMapper()
        val length = SUShort(meta)
        val ep = SUShort(meta)
        meta.fromBytes(DataBuffer(headerSlice.array().asUByteArray()))
        val packetLength = length.get()
        data = ByteBuffer.allocate(packetLength.toInt())
    }

    fun clear() {
        data = null
    }
}

class PPoGPebblePacketAssemblyException(message: String) : Exception(message)