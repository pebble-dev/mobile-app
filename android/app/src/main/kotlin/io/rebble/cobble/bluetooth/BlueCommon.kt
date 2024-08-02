package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import io.rebble.cobble.bluetooth.classic.BlueSerialDriver
import io.rebble.cobble.bluetooth.gatt.PPoGATTServer
import io.rebble.cobble.bluetooth.scan.BleScanner
import io.rebble.cobble.bluetooth.scan.ClassicScanner
import io.rebble.cobble.datasources.FlutterPreferences
import io.rebble.cobble.datasources.IncomingPacketsListener
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
        private val protocolHandler: ProtocolHandler,
        private val flutterPreferences: FlutterPreferences,
        private val incomingPacketsListener: IncomingPacketsListener
) {
    private var driver: BlueIO? = null

    private var externalIncomingPacketHandler: (suspend (ByteArray) -> Unit)? = null
    private var leServer: PPoGATTServer? = null

    fun startSingleWatchConnection(macAddress: String): Flow<SingleConnectionStatus> {
        bleScanner.stopScan()
        classicScanner.stopScan()

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress)

        Timber.d("Found Pebble device $bluetoothDevice'")

        val driver = getTargetTransport(bluetoothDevice)
        this@BlueCommon.driver = driver

        return driver.startSingleWatchConnection(bluetoothDevice)
    }

    fun getTargetTransport(device: BluetoothDevice): BlueIO {
        return when {
            device.type == BluetoothDevice.DEVICE_TYPE_LE -> { // LE only device
                if (leServer == null) {
                    leServer = PPoGATTServer(context)
                }
                BlueLEDriver(context, this.leServer!!, protocolHandler, flutterPreferences, incomingPacketsListener)
            }
            device.type != BluetoothDevice.DEVICE_TYPE_UNKNOWN -> { // Serial only device or serial/LE
                BlueSerialDriver(
                        protocolHandler,
                        incomingPacketsListener
                )
            }
            else -> throw IllegalArgumentException("Unknown device type: ${device.type}") // Can't contact device
        }
    }
}