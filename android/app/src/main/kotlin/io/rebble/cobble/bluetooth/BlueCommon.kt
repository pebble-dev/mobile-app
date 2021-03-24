package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import io.rebble.cobble.bluetooth.classic.BlueSerialDriver
import io.rebble.cobble.bluetooth.scan.BleScanner
import io.rebble.cobble.bluetooth.scan.ClassicScanner
import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlueCommon @Inject constructor(
        private val context: Context,
        private val bleScanner: BleScanner,
        private val classicScanner: ClassicScanner,
        private val protocolHandler: ProtocolHandler
) {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var driver: BlueIO? = null

    private var externalIncomingPacketHandler: (suspend (ByteArray) -> Unit)? = null

    fun startSingleWatchConnection(macAddress: String): Flow<SingleConnectionStatus> {
        bleScanner.stopScan()
        classicScanner.stopScan()

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        this@BlueCommon.bluetoothAdapter = bluetoothAdapter

        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress)

        Timber.d("Found Pebble device $bluetoothDevice'")

        val driver = getTargetTransport(bluetoothDevice)
        this@BlueCommon.driver = driver

        return driver.startSingleWatchConnection(bluetoothDevice)
    }

    fun getTargetTransport(device: BluetoothDevice): BlueIO {
        return when {
            device.type == BluetoothDevice.DEVICE_TYPE_LE -> { // LE only device
                BlueLEDriver(context, protocolHandler)
            }
            device.type != BluetoothDevice.DEVICE_TYPE_UNKNOWN -> { // Serial only device or serial/LE
                BlueSerialDriver(
                        protocolHandler
                )
            }
            else -> throw IllegalArgumentException("Unknown device type: ${device.type}") // Can't contact device
        }
    }
}