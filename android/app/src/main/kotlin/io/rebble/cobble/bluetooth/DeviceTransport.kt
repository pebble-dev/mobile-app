package io.rebble.cobble.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.companion.CompanionDeviceManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import io.rebble.cobble.BuildConfig
import io.rebble.cobble.bluetooth.ble.BlueLEDriver
import io.rebble.cobble.bluetooth.ble.GattServerManager
import io.rebble.cobble.bluetooth.classic.BlueSerialDriver
import io.rebble.cobble.bluetooth.classic.SocketSerialDriver
import io.rebble.cobble.bluetooth.scan.BleScanner
import io.rebble.cobble.bluetooth.scan.ClassicScanner
import io.rebble.cobble.datasources.FlutterPreferences
import io.rebble.cobble.datasources.IncomingPacketsListener
import io.rebble.libpebblecommon.ProtocolHandler
import io.rebble.libpebblecommon.protocolhelpers.ProtocolEndpoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceTransport @Inject constructor(
        private val context: Context,
        private val bleScanner: BleScanner,
        private val classicScanner: ClassicScanner,
        private val protocolHandler: ProtocolHandler,
        private val flutterPreferences: FlutterPreferences,
        private val incomingPacketsListener: IncomingPacketsListener
) {
    private var driver: BlueIO? = null

    private val gattServerManager: GattServerManager = GattServerManager(context)

    private var externalIncomingPacketHandler: (suspend (ByteArray) -> Unit)? = null
    private var lastMacAddress: String? = null

    @OptIn(FlowPreview::class)
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun startSingleWatchConnection(macAddress: String): Flow<SingleConnectionStatus> {
        bleScanner.stopScan()
        classicScanner.stopScan()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val companionDeviceManager = context.getSystemService(CompanionDeviceManager::class.java)
            Timber.d("Companion device associated: ${macAddress in companionDeviceManager.associations}, associations: ${companionDeviceManager.associations}")
            lastMacAddress?.let {
                companionDeviceManager.stopObservingDevicePresence(it)
            }
            lastMacAddress = macAddress
            companionDeviceManager.startObservingDevicePresence(macAddress)
        }

        val bluetoothDevice = if (BuildConfig.DEBUG && !macAddress.contains(":")) {
            PebbleDevice(null, true, macAddress)
        } else {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            PebbleDevice(bluetoothAdapter.getRemoteDevice(macAddress))
        }

        val driver = getTargetTransport(bluetoothDevice)
        this@DeviceTransport.driver = driver
        return driver.startSingleWatchConnection(bluetoothDevice)
    }

    @Throws(SecurityException::class)
    private fun getTargetTransport(pebbleDevice: PebbleDevice): BlueIO {
        val btDevice = pebbleDevice.bluetoothDevice
        return when {
            pebbleDevice.emulated -> {
                SocketSerialDriver(
                        protocolHandler,
                        incomingPacketsListener.receivedPackets
                )
            }
            btDevice?.type == BluetoothDevice.DEVICE_TYPE_LE || btDevice?.type == BluetoothDevice.DEVICE_TYPE_UNKNOWN /* || btDevice?.type == BluetoothDevice.DEVICE_TYPE_DUAL */-> { // LE device
                gattServerManager.initIfNeeded()
                if (btDevice.type == BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
                    Timber.w("Device $pebbleDevice has type unknown, assuming LE will work")
                }
                BlueLEDriver(
                        context = context,
                        protocolHandler = protocolHandler,
                        gattServerManager = gattServerManager,
                        incomingPacketsListener = incomingPacketsListener.receivedPackets,
                ) {
                    flutterPreferences.shouldActivateWorkaround(it)
                }
            }
            btDevice?.type == BluetoothDevice.DEVICE_TYPE_CLASSIC || btDevice?.type == BluetoothDevice.DEVICE_TYPE_DUAL -> { // Serial only device or serial/LE
                BlueSerialDriver(
                        protocolHandler,
                        incomingPacketsListener.receivedPackets
                )
            }
            else -> throw IllegalArgumentException("Unknown device type: ${btDevice?.type}") // Can't contact device
        }
    }
}