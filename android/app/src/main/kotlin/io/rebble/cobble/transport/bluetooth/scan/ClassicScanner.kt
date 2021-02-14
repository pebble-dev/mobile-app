package io.rebble.cobble.transport.bluetooth.scan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.IntentFilter
import io.rebble.cobble.transport.bluetooth.BluePebbleDevice
import io.rebble.cobble.util.coroutines.asFlow
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.selects.select
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ClassicScanner @Inject constructor(private val context: Context) {
    private var stopTrigger: CompletableDeferred<Unit>? = null

    fun getScanFlow(): Flow<List<BluePebbleDevice>> = flow {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                ?: throw BluetoothNotSupportedException("Device does not have a bluetooth adapter")

        coroutineScope {
            var deviceList = emptyList<BluePebbleDevice>()
            val stopTrigger = CompletableDeferred<Unit>()
            this@ClassicScanner.stopTrigger = stopTrigger

            val foundDevicesChannel = IntentFilter(BluetoothDevice.ACTION_FOUND)
                    .asFlow(context).produceIn(this)
            val scanningFinishChannel = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                    .asFlow(context).produceIn(this)

            try {
                val scanStarted = bluetoothAdapter.startDiscovery()
                if (!scanStarted) {
                    throw IllegalStateException("Scan failed to start")
                }

                val scanEndTime = System.currentTimeMillis() + SCAN_TIMEOUT_MS

                var keepScanning = true
                while (keepScanning) {
                    select<Unit> {
                        foundDevicesChannel.onReceive { intent ->
                            val device = intent.getParcelableExtra<BluetoothDevice>(
                                    BluetoothDevice.EXTRA_DEVICE
                            ) ?: return@onReceive

                            val name = device.name ?: return@onReceive
                            if (name.startsWith("Pebble") &&
                                    !name.contains("LE") &&
                                    !deviceList.any {
                                        it.bluetoothDevice.address == device.address
                                    }) {
                                deviceList = deviceList + BluePebbleDevice(device)
                                emit(deviceList)
                            }
                        }
                        scanningFinishChannel.onReceive {
                            keepScanning = false
                        }
                        stopTrigger.onAwait {
                            keepScanning = false
                        }
                        onTimeout(scanEndTime - System.currentTimeMillis()) {
                            keepScanning = false
                        }
                    }
                }
            } finally {
                foundDevicesChannel.cancel()
                scanningFinishChannel.cancel()
                bluetoothAdapter.cancelDiscovery()
                this@ClassicScanner.stopTrigger = null
            }
        }
    }

    fun stopScan() {
        stopTrigger?.complete(Unit)
    }
}

private val SCAN_TIMEOUT_MS = 8_000L