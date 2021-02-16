package io.rebble.cobble.transport.emulator

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import io.rebble.cobble.transport.ProtocolIO
import io.rebble.cobble.transport.bluetooth.SingleConnectionStatus
import io.rebble.cobble.transport.bluetooth.readFully
import io.rebble.cobble.util.toHexString
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.packets.HEADER_SIGNATURE
import io.rebble.libpebblecommon.packets.QemuPacket
import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import io.rebble.libpebblecommon.protocolhelpers.ProtocolEndpoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@Suppress("BlockingMethodInNonBlockingContext")
class EmuSocketDriver @Inject constructor(private val protocolHandler: ProtocolHandler) {
    private val fakeDevice: BluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("EE:EE:EE:EE:EE:EE")

    private suspend fun readLoop(inputStream: InputStream) {
        try {
            val buf: ByteBuffer = ByteBuffer.allocate(8192)

            while (coroutineContext.isActive) {
                /* READ PACKET META */
                inputStream.readFully(buf, 0, 2)
                val metBuf = ByteBuffer.wrap(buf.array())
                metBuf.order(ByteOrder.BIG_ENDIAN)
                val sig = metBuf.short
                if (sig.toUShort() != HEADER_SIGNATURE.toUShort()) {
                    continue
                }
                inputStream.readFully(buf, 2, 4)
                val protocol = metBuf.short
                val emuLen = metBuf.short
                inputStream.readFully(buf, 6, emuLen.toInt())
                val packet = ByteArray(emuLen.toInt() + 4 * (Short.SIZE_BYTES))
                buf.get(packet,0,packet.size)
                val qemuPacket = QemuPacket.deserialize(packet.toUByteArray())
                if (qemuPacket !is QemuPacket.QemuSPP) {
                    Timber.d("QEMU packet with protocol ${qemuPacket.protocol.get()} ignored")
                    continue
                }
                protocolHandler.receivePacket(qemuPacket.payload.get())
                buf.rewind()
            }
        } finally {
            Timber.e("Read loop returning")
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun write(outputStream: OutputStream, bytes: ByteArray) = withContext(Dispatchers.IO) {
        //TODO: remove msg
        Timber.d("Sending packet of EP ${PebblePacket(bytes.toUByteArray()).endpoint}")
        outputStream.write(QemuPacket.QemuSPP(bytes.toUByteArray()).serialize().toByteArray())
    }


    fun startEmulatorConnection(host: String, port: Int): Flow<SingleConnectionStatus> = flow {
        coroutineScope {
            emit(SingleConnectionStatus.Connecting(fakeDevice))
            val socket = withContext(Dispatchers.IO) {
                Socket(InetAddress.getByName(host), port)
            }
            val outputStream = socket.getOutputStream()
            val sendLoop = launch {
                protocolHandler.startPacketSendingLoop {
                    write(outputStream, it.toByteArray())
                    return@startPacketSendingLoop true
                }
            }
            emit(SingleConnectionStatus.Connected(fakeDevice))
            readLoop(socket.getInputStream())
            try {
                socket.close()
            }catch (e: IOException) {}
            sendLoop.cancel()
        }
    }
}