package io.rebble.fossil.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import io.flutter.Log
import io.rebble.libpebblecommon.BluetoothConnection
import kotlinx.coroutines.CoroutineExceptionHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlueCommon @Inject constructor(
        private val context: Context,
        private val coroutineExceptionHandler: CoroutineExceptionHandler
) : BroadcastReceiver(), BluetoothConnection {
    private val logTag = "BlueCommon"

    val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private var pebbleList: MutableList<BluePebbleDevice> = mutableListOf()
    private val scanHandler = Handler()
    var driver: BlueIO? = null

    private var isScanning = false
    private var scanRetries = 0
    private var onConChange: ((Boolean) -> Unit)? = null
    private var resultCallback: ((BluePebbleDevice) -> Unit)? = null

    private var externalIncomingPacketHandler: (suspend (ByteArray) -> Unit)? = null

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action!!) {
            BluetoothDevice.ACTION_FOUND -> {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                val deviceName = if (device.name == null) "" else device.name
                val deviceHardwareAddress = device.address
                if (deviceName.startsWith("Pebble") && !deviceName.contains("LE")) {
                    Log.d(logTag, "Found Pebble in scan: $deviceName")
                    if (pebbleList.size > 0) {
                        if (pebbleList.firstOrNull { p -> p.bluetoothDevice.address == deviceHardwareAddress } == null) {
                            pebbleList.add(BluePebbleDevice(device))
                        }
                    } else {
                        pebbleList.add(BluePebbleDevice(device))
                    }
                }
            }
        }
    }

    fun scanDevicesClassic(resultCallback: (List<BluePebbleDevice>) -> Unit) {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        pebbleList.clear()
        Log.d(logTag, "Scanning for pebbles")
        context.registerReceiver(this, filter)
        // Scan for 8 seconds, then stop and send back what we found
        //TODO: Could make this live-update the list?
        if (bluetoothAdapter.startDiscovery() || isScanning) {
            isScanning = true
            scanHandler.postDelayed({
                if (pebbleList.size > 0 || scanRetries >= 3) {
                    scanRetries = 0
                    Log.d(logTag, "Scanning ended")
                    isScanning = false
                    bluetoothAdapter.cancelDiscovery()
                    try {
                        context.unregisterReceiver(this)
                    } catch (e: IllegalArgumentException) {
                        // Receiver was not registered in the first place. Do nothing.
                    }
                    resultCallback(pebbleList)
                } else {
                    scanRetries++
                    scanDevicesClassic(resultCallback)
                }
            }, 8000)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (result.device.name != null && result.device.type == BluetoothDevice.DEVICE_TYPE_LE && (result.device.name.startsWith("Pebble ") || result.device.name.startsWith("Pebble-LE"))) {
                resultCallback?.invoke(BluePebbleDevice(result))
            }
        }
    }

    private val legacyLeScanCallback = object : BluetoothAdapter.LeScanCallback {
        override fun onLeScan(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?) {
            if (device != null && device.type == BluetoothDevice.DEVICE_TYPE_LE && scanRecord != null) {
                if (device.name != null && (device.name.startsWith("Pebble ") || device.name.startsWith("Pebble-LE"))) {
                    resultCallback?.invoke(BluePebbleDevice(device, scanRecord))
                }
            }
        }

    }

    private fun startLEScan() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner.startScan(leScanCallback)
        }else {
            bluetoothAdapter.startLeScan(legacyLeScanCallback)
        }
    }

    private fun stopLEScan() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner.stopScan(leScanCallback)
        }else {
            bluetoothAdapter.stopLeScan(legacyLeScanCallback)
        }
    }

    fun scanDevicesLE(resultCallback: (BluePebbleDevice) -> Unit, endCallback: () -> Unit) {
        this.resultCallback = resultCallback
        pebbleList.clear()
        Log.d(logTag, "Scanning for LE pebbles")
        scanHandler.postDelayed({
            if (pebbleList.size > 0 || scanRetries >= 3) {
                scanRetries = 0
                isScanning = false
                stopLEScan()
                endCallback()
            }else {
                scanRetries++
                scanDevicesLE(resultCallback, endCallback)
            }
        }, 8000)
        isScanning = true
        startLEScan()
    }

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
        return when {
            device.type == BluetoothDevice.DEVICE_TYPE_LE -> { // LE only device
                scanHandler.removeCallbacksAndMessages(null);
                stopLEScan()
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