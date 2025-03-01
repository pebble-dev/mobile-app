package io.rebble.cobble.bluetooth.classic

import android.Manifest
import androidx.annotation.RequiresPermission
import io.rebble.cobble.bluetooth.BlueIO
import io.rebble.cobble.bluetooth.BluetoothPebbleDevice
import io.rebble.cobble.bluetooth.ProtocolIO
import io.rebble.cobble.bluetooth.SingleConnectionStatus
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.domain.common.PebbleDevice
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.util.UUID

@Suppress("BlockingMethodInNonBlockingContext")
class BlueSerialDriver(
    private val protocolHandler: PebbleDevice,
    private val incomingPacketsListener: MutableSharedFlow<ByteArray>
) : BlueIO {
    private var protocolIO: ProtocolIO? = null

    @FlowPreview
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun startSingleWatchConnection(device: PebbleDevice): Flow<SingleConnectionStatus> =
        flow<SingleConnectionStatus> {
            require(device is BluetoothPebbleDevice) { "Device must be BluetoothPebbleDevice" }
            emit(SingleConnectionStatus.Connecting(device))

            val btSerialUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            val serialSocket =
                withContext(Dispatchers.IO) {
                    device.bluetoothDevice.createRfcommSocketToServiceRecord(btSerialUUID).also {
                        it.connect()
                    }
                }

            val sendLoop =
                device.negotiationScope.launch(CoroutineName("SendLoop")) {
                    device.protocolHandler.startPacketSendingLoop(::sendPacket)
                }
            sendLoop.invokeOnCompletion {
                Logging.e("Send loop completed", it)
                serialSocket.close()
            }

            emit(SingleConnectionStatus.Connected(device))

            protocolIO =
                ProtocolIO(
                    serialSocket.inputStream,
                    serialSocket.outputStream,
                    device.protocolHandler,
                    incomingPacketsListener
                )

            protocolIO!!.readLoop()
            try {
                Logging.e(
                    "Closing socket post read loop: isConnected = ${serialSocket.isConnected}"
                )
                serialSocket?.close()
            } catch (e: IOException) {
            }
            sendLoop.cancel()
        }

    private suspend fun sendPacket(bytes: UByteArray): Boolean {
        val protocolIO = protocolIO ?: return false
        protocolIO.write(bytes.toByteArray())
        return true
    }
}