package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import io.rebble.fossil.bluetooth.scan.BleScanner
import io.rebble.fossil.bluetooth.scan.ClassicScanner
import io.rebble.libpebblecommon.BluetoothConnection
import kotlinx.coroutines.CoroutineExceptionHandler
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

    private var onConChange: ((Boolean) -> Unit)? = null

    private var externalIncomingPacketHandler: (suspend (ByteArray) -> Unit)? = null

    fun targetPebble(addr: Long): Boolean {
        val hex = "%X".format(addr).padStart(12, '0')
        var btaddr = ""
        for (i in hex.indices) {
            btaddr += hex[i]
            if ((i + 1) % 2 == 0 && i + 1 < hex.length) btaddr += ":"
        }

        val targetPebble = bluetoothAdapter.getRemoteDevice(btaddr)
        return targetPebble(targetPebble)
    }

    fun targetPebble(device: BluetoothDevice): Boolean {
        bleScanner.stopScan()
        classicScanner.stopScan()

        return when {
            device.type == BluetoothDevice.DEVICE_TYPE_LE -> { // LE only device
                driver = BlueLEDriver(device, context, this::handlePacketReceivedFromDriver)
                onConChange?.let { driver!!.setOnConnectionChange(it) }
                driver!!.connectPebble()
            }
            device.type != BluetoothDevice.DEVICE_TYPE_UNKNOWN -> { // Serial only device or serial/LE
                driver = BlueSerialDriver(
                        device,
                        bluetoothAdapter,
                        context,
                        coroutineExceptionHandler,
                        this::handlePacketReceivedFromDriver
                )

                onConChange?.let { driver!!.setOnConnectionChange(it) }
                driver!!.connectPebble()
            }
            else -> false // Can't contact device
        }
    }

    fun setOnConnectionChange(f: (Boolean) -> Unit) {
        onConChange = f
        driver?.setOnConnectionChange(f)
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