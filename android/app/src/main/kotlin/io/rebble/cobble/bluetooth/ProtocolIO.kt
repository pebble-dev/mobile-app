package io.rebble.cobble.bluetooth

import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.coroutineContext

@Suppress("BlockingMethodInNonBlockingContext")
class ProtocolIO(private val inputStream: InputStream, private val outputStream: OutputStream, private val protocolHandler: ProtocolHandler) {
    suspend fun readLoop() {
        try {
            val buf: ByteBuffer = ByteBuffer.allocate(8192)

            while (coroutineContext.isActive) {
                /* READ PACKET META */
                inputStream.readFully(buf, 0, 4)
                val metBuf = ByteBuffer.wrap(buf.array())
                metBuf.order(ByteOrder.BIG_ENDIAN)
                val length = metBuf.short
                val endpoint = metBuf.short
                if (length < 0 || length > buf.capacity()) {
                    Timber.w("Invalid length in packet (EP ${endpoint.toUShort()}): got ${length.toUShort()}")
                    continue
                }

                /* READ PACKET CONTENT */
                inputStream.readFully(buf, 4, length.toInt())

                Timber.d("Got packet: EP ${endpoint.toUShort()} | Length ${length.toUShort()}")

                buf.rewind()
                val packet = ByteArray(length.toInt() + 2 * (Short.SIZE_BYTES))
                buf.get(packet, 0, packet.size)
                protocolHandler.receivePacket(packet.toUByteArray())
            }
        } finally {
            try {
                inputStream.close()
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    suspend fun write(bytes: ByteArray) = withContext(Dispatchers.IO) {
        outputStream.write(bytes)
    }
}
