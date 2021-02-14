package io.rebble.cobble.transport.bluetooth.classic

import android.bluetooth.BluetoothDevice
import io.rebble.cobble.transport.bluetooth.BlueIO
import io.rebble.cobble.transport.ProtocolIO
import io.rebble.cobble.transport.bluetooth.SingleConnectionStatus
import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
class BlueSerialDriver(
        private val protocolHandler: ProtocolHandler
) : BlueIO {
    private var protocolIO: ProtocolIO? = null

    override fun startSingleWatchConnection(device: BluetoothDevice): Flow<SingleConnectionStatus> = flow {
        coroutineScope {
            emit(SingleConnectionStatus.Connecting(device))

            val btSerialUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            val serialSocket = withContext(Dispatchers.IO) {
                device.createRfcommSocketToServiceRecord(btSerialUUID).also {
                    it.connect()
                }
            }

            val sendLoop = launch {
                protocolHandler.startPacketSendingLoop(::sendPacket)
            }

            emit(SingleConnectionStatus.Connected(device))

            protocolIO = ProtocolIO(serialSocket.inputStream, serialSocket.outputStream, protocolHandler)

            protocolIO!!.readLoop()
            try {
                serialSocket?.close()
            } catch (e: IOException) {
            }
            sendLoop.cancel()
        }
    }

    private suspend fun sendPacket(bytes: UByteArray): Boolean {
        val protocolIO = protocolIO ?: return false
        @Suppress("BlockingMethodInNonBlockingContext")
        protocolIO.write(bytes.toByteArray())
        return true
    }

}