package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import io.rebble.fossil.bluetooth.classic.BlueSerialDriver
import io.rebble.fossil.bluetooth.scan.BleScanner
import io.rebble.fossil.bluetooth.scan.ClassicScanner
import io.rebble.libpebblecommon.BluetoothConnection
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlueCommon @Inject constructor(
        private val context: Context,
        private val coroutineExceptionHandler: CoroutineExceptionHandler,
        private val bleScanner: BleScanner,
        private val classicScanner: ClassicScanner
) : BluetoothConnection {
    private val logTag = "BlueCommon"

    val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
    var driver: BlueIO? = null

    private var externalIncomingPacketHandler: (suspend (ByteArray) -> Unit)? = null

    fun startSingleWatchConnection(macAddress: Long): Flow<SingleConnectionStatus> {
        val hex = "%X".format(macAddress).padStart(12, '0')
        var btaddr = ""
        for (i in hex.indices) {
            btaddr += hex[i]
            if ((i + 1) % 2 == 0 && i + 1 < hex.length) btaddr += ":"
        }

        return startSingleWatchConnection(btaddr)
    }

    fun startSingleWatchConnection(macAddress: String): Flow<SingleConnectionStatus> {
        bleScanner.stopScan()
        classicScanner.stopScan()

        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress)

        Log.d(logTag, "Found Pebble device $bluetoothDevice'")

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
        externalIncomingPacketHandler?.invoke(packet)
    }
}