package io.rebble.fossil

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import io.flutter.Log

class BlueCommon(private val context: Context, private val packetCallback: (ByteArray) -> Unit) : BroadcastReceiver() {
    private val logTag = "BlueCommon"

    val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
    private var pebbleList: MutableList<BluetoothDevice> = mutableListOf()
    private val scanHandler = Handler()
    var driver: BlueIO? = null

    private var isScanning = false
    private var scanRetries = 0
    private var onConChange: ((Boolean) -> Unit)? = null

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action!!) {
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
                        if(pebbleList.firstOrNull { p -> p.address == deviceHardwareAddress } == null) {
                            pebbleList.add(device)
                        }
                    }else{
                        pebbleList.add(device)
                    }
                }
            }
        }
    }

    fun scanDevices(resultCallback: (List<BluetoothDevice>) -> Unit) {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        pebbleList.clear()
        Log.d(logTag, "Scanning for pebbles")
        context.registerReceiver(this, filter)
        // Scan for 8 seconds, then stop and send back what we found
        //TODO: Could make this live-update the list?
        if(bluetoothAdapter.startDiscovery() || isScanning) {
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
                }else {
                    scanRetries++
                    scanDevices(resultCallback)
                }
            }, 8000)
        }
    }

    fun targetPebble(addr: Long): Boolean {
        val hex = "%X".format(addr).padStart(12, '0')
        var btaddr = ""
        for (i in hex.indices) {
            btaddr += hex[i]
            if ((i+1) % 2 == 0 && i+1 < hex.length) btaddr += ":"
        }

        val targetPebble = bluetoothAdapter.getRemoteDevice(btaddr)
        return targetPebble(targetPebble)
    }

    fun targetPebble(device: BluetoothDevice): Boolean {
        return when {
            device.type == BluetoothDevice.DEVICE_TYPE_LE -> { // LE only device
                TODO("BLE")
            }
            device.type != BluetoothDevice.DEVICE_TYPE_UNKNOWN -> { // Serial only device or serial/LE
                driver = BlueSerial(bluetoothAdapter, context, packetCallback)
                onConChange?.let { driver!!.setOnConnectionChange(it) }
                driver!!.targetPebble(device)
            }
            else -> false // Can't contact device
        }
    }

    fun setOnConnectionChange(f: (Boolean) -> Unit) {
        onConChange = f
        driver?.setOnConnectionChange(f)
    }
}