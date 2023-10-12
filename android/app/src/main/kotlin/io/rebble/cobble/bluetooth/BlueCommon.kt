package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import io.rebble.cobble.BuildConfig
import io.rebble.cobble.bluetooth.classic.BlueSerialDriver
import io.rebble.cobble.bluetooth.classic.SocketSerialDriver
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

    fun startSingleWatchConnection(macAddress: String): Flow<SingleConnectionStatus> {
        bleScanner.stopScan()
        classicScanner.stopScan()
        val bluetoothDevice = if (BuildConfig.DEBUG && !macAddress.contains(":")) {
            PebbleBluetoothDevice(null, true, macAddress)
        } else {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            PebbleBluetoothDevice(bluetoothAdapter.getRemoteDevice(macAddress))
        }

        val driver = getTargetTransport(bluetoothDevice)
        this@BlueCommon.driver = driver

        return driver.startSingleWatchConnection(bluetoothDevice)
    }

    private fun getTargetTransport(pebbleDevice: PebbleBluetoothDevice): BlueIO {
        val btDevice = pebbleDevice.bluetoothDevice
        return when {
            pebbleDevice.emulated -> {
                SocketSerialDriver(
                        protocolHandler,
                        incomingPacketsListener
                )
            }
            btDevice?.type == BluetoothDevice.DEVICE_TYPE_LE -> { // LE only device
                BlueLEDriver(context, protocolHandler, flutterPreferences, incomingPacketsListener)
            }
            btDevice?.type != BluetoothDevice.DEVICE_TYPE_UNKNOWN -> { // Serial only device or serial/LE
                BlueSerialDriver(
                        protocolHandler,
                        incomingPacketsListener
                )
            }
            else -> throw IllegalArgumentException("Unknown device type: ${btDevice?.type}") // Can't contact device
        }
    }
}