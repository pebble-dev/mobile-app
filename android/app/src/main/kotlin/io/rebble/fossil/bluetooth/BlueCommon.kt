package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import io.rebble.fossil.bluetooth.classic.BlueSerialDriver
import io.rebble.fossil.bluetooth.scan.BleScanner
import io.rebble.fossil.bluetooth.scan.ClassicScanner
import io.rebble.libpebblecommon.BluetoothConnection
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlueCommon @Inject constructor(
        private val context: Context,
        private val coroutineExceptionHandler: CoroutineExceptionHandler,
        private val bleScanner: BleScanner,
        private val classicScanner: ClassicScanner
) : BluetoothConnection {
    val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
    var driver: BlueIO? = null

    private var externalIncomingPacketHandler: (suspend (ByteArray) -> Unit)? = null

    fun startSingleWatchConnection(macAddress: String): Flow<SingleConnectionStatus> {
        bleScanner.stopScan()
        classicScanner.stopScan()

        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress)

        Timber.d("Found Pebble device $bluetoothDevice'")

        val driver = getTargetTransport(bluetoothDevice)
        this@BlueCommon.driver = driver

        return driver.startSingleWatchConnection(bluetoothDevice)
    }

    fun getTargetTransport(device: BluetoothDevice): BlueIO {
        return when {
            device.type == BluetoothDevice.DEVICE_TYPE_LE -> { // LE only device
                BlueLEDriver(device, context, this::handlePacketReceivedFromDriver)
            }
            device.type != BluetoothDevice.DEVICE_TYPE_UNKNOWN -> { // Serial only device or serial/LE
                BlueSerialDriver(
                        context,
                        this::handlePacketReceivedFromDriver
                )
            }
            else -> throw IllegalArgumentException("Unknown device type: ${device.type}") // Can't contact device
        }
    }

    override suspend fun sendPacket(data: ByteArray) {
        driver?.sendPacket(data)
    }

    override fun setReceiveCallback(callback: suspend (ByteArray) -> Unit) {
        externalIncomingPacketHandler = callback
    }

    private suspend fun handlePacketReceivedFromDriver(packet: ByteArray) {
        try {
            externalIncomingPacketHandler?.invoke(packet)
        } catch (e: Exception) {
            Timber.e(e, "Packet receive failed")
        }
    }
}