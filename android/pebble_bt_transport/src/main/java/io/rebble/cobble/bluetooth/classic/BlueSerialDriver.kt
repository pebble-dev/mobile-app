package io.rebble.cobble.bluetooth.classic

import android.Manifest
import androidx.annotation.RequiresPermission
import io.rebble.cobble.bluetooth.*
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.util.UUID

@Suppress("BlockingMethodInNonBlockingContext")
class BlueSerialDriver(
        private val protocolHandler: ProtocolHandler,
        private val incomingPacketsListener: MutableSharedFlow<ByteArray>
) : BlueIO {
    private var protocolIO: ProtocolIO? = null

    @FlowPreview
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun startSingleWatchConnection(device: PebbleDevice): Flow<SingleConnectionStatus> = flow {
        require(device is BluetoothPebbleDevice) { "Device must be BluetoothPebbleDevice" }
        coroutineScope {
            emit(SingleConnectionStatus.Connecting(device))

            val btSerialUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            val serialSocket = withContext(Dispatchers.IO) {
                device.bluetoothDevice.createRfcommSocketToServiceRecord(btSerialUUID).also {
                    it.connect()
                }
            }

            val sendLoop = launch(CoroutineName("SendLoop")) {
                protocolHandler.startPacketSendingLoop(::sendPacket)
            }

            emit(SingleConnectionStatus.Connected(device))

            protocolIO = ProtocolIO(
                    serialSocket.inputStream,
                    serialSocket.outputStream,
                    protocolHandler,
                    incomingPacketsListener
            )

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
        protocolIO.write(bytes.toByteArray())
        return true
    }

}