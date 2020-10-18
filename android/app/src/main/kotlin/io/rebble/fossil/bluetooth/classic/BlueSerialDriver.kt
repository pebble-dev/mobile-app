package io.rebble.fossil.bluetooth.classic

import android.bluetooth.BluetoothDevice
import android.content.Context
import io.rebble.fossil.bluetooth.BlueIO
import io.rebble.fossil.bluetooth.SingleConnectionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Suppress("BlockingMethodInNonBlockingContext")
class BlueSerialDriver(
        private val context: Context,
        private val packetCallback: suspend (ByteArray) -> Unit
) : BlueIO {
    private var outputStream: OutputStream? = null

    override fun startSingleWatchConnection(device: BluetoothDevice): Flow<SingleConnectionStatus> = flow {
        emit(SingleConnectionStatus.Connecting(device))

        val btSerialUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
        val serialSocket = withContext(Dispatchers.IO) {
            device.createRfcommSocketToServiceRecord(btSerialUUID).also {
                it.connect()
            }
        }

        emit(SingleConnectionStatus.Connected(device))

        val inputStream = serialSocket.inputStream
        outputStream = serialSocket.outputStream

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
                    Timber.w("Invalid length in packet (EP $endpoint): got $length")
                    continue
                }

                /* READ PACKET CONTENT */
                inputStream.readFully(buf, 4, length.toInt())

                Timber.d("Got packet: EP $endpoint | Length $length")

                buf.rewind()
                val packet = ByteArray(length.toInt() + 2 * (Short.SIZE_BYTES))
                buf.get(packet, 0, packet.size)
                packetCallback.invoke(packet)
            }
        } finally {
            try {
                serialSocket?.close()
                inputStream.close()
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            outputStream = null
        }
    }

    override suspend fun sendPacket(bytes: ByteArray): Boolean {
        val outputStream = outputStream ?: return false
        @Suppress("BlockingMethodInNonBlockingContext")
        withContext(Dispatchers.IO) {
            outputStream.write(bytes)
        }

        return true
    }

    private suspend fun InputStream.readFully(buffer: ByteBuffer, offset: Int, count: Int) {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine<Unit> { continuation ->
                continuation.invokeOnCancellation {
                    close()
                }

                try {
                    var totalRead = 0
                    while (coroutineContext.isActive && totalRead < count) {
                        val read = read(buffer.array(), offset + totalRead, count - totalRead)
                        if (read < 0) {
                            throw IOException("Reached end of stream")
                        }

                        totalRead += read
                    }

                    continuation.resume(Unit)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }

}