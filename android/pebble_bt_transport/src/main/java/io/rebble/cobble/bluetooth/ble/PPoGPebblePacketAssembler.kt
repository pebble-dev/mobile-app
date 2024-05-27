package io.rebble.cobble.bluetooth.ble

import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import io.rebble.libpebblecommon.structmapper.SUShort
import io.rebble.libpebblecommon.structmapper.StructMapper
import io.rebble.libpebblecommon.util.DataBuffer
import kotlinx.coroutines.flow.flow
import java.nio.ByteBuffer
import kotlin.math.min

@OptIn(ExperimentalUnsignedTypes::class)
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
                val header = ByteArray(4)
                dataToAddBuf.get(header)
                beginAssembly(header)
            }

            val remaining = min(dataToAddBuf.remaining(), data!!.remaining())
            val slice = ByteArray(remaining)
            dataToAddBuf.get(slice)
            data!!.put(slice)

            if (!data!!.hasRemaining()) {
                data!!.flip()
                val packet = PebblePacket.deserialize(data!!.array().asUByteArray())
                emit(packet)
                clear()
            }
        }
    }

    private fun beginAssembly(header: ByteArray) {
        val meta = StructMapper()
        val length = SUShort(meta)
        val ep = SUShort(meta)
        meta.fromBytes(DataBuffer(header.asUByteArray()))
        val packetLength = length.get()
        data = ByteBuffer.allocate(packetLength.toInt()+4)
        data!!.put(header)
    }

    fun clear() {
        data = null
    }
}

class PPoGPebblePacketAssemblyException(message: String) : Exception(message)