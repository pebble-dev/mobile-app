package io.rebble.cobble.bluetooth.classic

import io.rebble.cobble.bluetooth.BlueIO
import io.rebble.cobble.bluetooth.EmulatedPebbleDevice
import io.rebble.cobble.bluetooth.SingleConnectionStatus
import io.rebble.cobble.bluetooth.readFully
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.libpebblecommon.packets.QemuPacket
import io.rebble.libpebblecommon.protocolhelpers.ProtocolEndpoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.coroutineContext

/**
 * Used for testing app via a qemu pebble
 */
class SocketSerialDriver(
    private val device: PebbleDevice,
    private val incomingPacketsListener: MutableSharedFlow<ByteArray>
) : BlueIO {
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private suspend fun readLoop() {
        try {
            val buf: ByteBuffer = ByteBuffer.allocate(8192)

            while (coroutineContext.isActive) {
                val inputStream = inputStream ?: break
                // READ PACKET META
                inputStream.readFully(buf, 0, 4)

                val qemuPacket = QemuPacket.deserialize(buf.array().asUByteArray())
                if (qemuPacket.protocol.get() != UShort.MAX_VALUE) {
                    Timber.d("QEMU packet ${qemuPacket.protocol.get()}")
                }
                val sppPacket = qemuPacket as? QemuPacket.QemuSPP ?: continue

                buf.rewind()
                inputStream.readFully(buf, 4, sppPacket.length.get().toInt())
                buf.rewind()

                val metBuf = ByteBuffer.wrap(buf.array())
                metBuf.order(ByteOrder.BIG_ENDIAN)
                val length = metBuf.short
                val endpoint = metBuf.short
                if (length < 0 || length > buf.capacity()) {
                    Timber.w(
                        "Invalid length in packet (EP ${endpoint.toUShort()}): got ${length.toUShort()}"
                    )
                    continue
                }

                Timber.d(
                    "Got packet: EP ${ProtocolEndpoint.getByValue(
                        endpoint.toUShort()
                    )} | Length ${length.toUShort()}"
                )

                buf.rewind()
                val packet = ByteArray(length.toInt() + 2 * (Short.SIZE_BYTES))
                buf.get(packet, 0, packet.size)
                incomingPacketsListener.emit(packet)
                device.protocolHandler.receivePacket(packet.toUByteArray())
            }
        } finally {
            Timber.e("Read loop returning")
            try {
                withContext(Dispatchers.IO) {
                    inputStream?.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                inputStream = null
            }

            try {
                withContext(Dispatchers.IO) {
                    outputStream?.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                outputStream = null
            }
        }
    }

    @FlowPreview
    override fun startSingleWatchConnection(device: PebbleDevice): Flow<SingleConnectionStatus> =
        flow {
            require(device is EmulatedPebbleDevice) { "Device must be EmulatedPebbleDevice" }
            val host = device.address
            coroutineScope {
                emit(SingleConnectionStatus.Connecting(device))

                val serialSocket =
                    withContext(Dispatchers.IO) {
                        Socket(host, 12344)
                    }

                delay(8000)

                val sendLoop =
                    launch {
                        device.protocolHandler.startPacketSendingLoop(::sendPacket)
                    }

                inputStream = serialSocket.inputStream
                outputStream = serialSocket.outputStream

                readLoop()
                try {
                    withContext(Dispatchers.IO) {
                        serialSocket.close()
                    }
                } catch (_: IOException) {
                }
                sendLoop.cancel()
            }
        }

    private suspend fun sendPacket(bytes: UByteArray): Boolean {
        // Timber.d("Sending packet of EP ${PebblePacket(bytes.toUByteArray()).endpoint}")
        val qemuPacket = QemuPacket.QemuSPP(bytes)
        val outputStream = outputStream ?: return false
        withContext(Dispatchers.IO) {
            outputStream.write(qemuPacket.serialize().toByteArray())
        }
        return true
    }
}