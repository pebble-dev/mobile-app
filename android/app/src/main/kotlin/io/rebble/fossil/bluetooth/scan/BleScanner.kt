package io.rebble.fossil.bluetooth.scan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import io.rebble.fossil.bluetooth.BluePebbleDevice
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.selects.select
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class BleScanner @Inject constructor() {
    private var stopTrigger: CompletableDeferred<Unit>? = null

    fun getScanFlow(): Flow<List<BluePebbleDevice>> = flow {
        coroutineScope {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    ?: throw BluetoothNotSupportedException("Device does not have a bluetooth adapter")

            val leScanner = bluetoothAdapter.bluetoothLeScanner
            val callback = ScanCallbackChannel()

            var foundDevices = emptyList<BluePebbleDevice>()

            val stopTrigger = CompletableDeferred<Unit>()
            this@BleScanner.stopTrigger = stopTrigger

            try {
                leScanner.startScan(callback)

                val scanEndTime = System.currentTimeMillis() + SCAN_TIMEOUT_MS

                var keepScanning = true
                while (keepScanning) {
                    select<Unit> {
                        callback.resultChannel.onReceive { result ->
                            val device = result.device
                            if (device.name != null &&
                                    device.type == BluetoothDevice.DEVICE_TYPE_LE &&
                                    (device.name.startsWith("Pebble ") ||
                                            device.name.startsWith("Pebble-LE")) &&
                                    !foundDevices.any { it.bluetoothDevice.address == device.address }) {

                                val bluePebbleDevice = BluePebbleDevice(result)
                                foundDevices = foundDevices + bluePebbleDevice
                                emit(foundDevices)
                            }
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
                callback.resultChannel.cancel()
                leScanner.stopScan(callback)
                this@BleScanner.stopTrigger = null
            }
        }
    }


    fun stopScan() {
        stopTrigger?.complete(Unit)
    }

    private class ScanCallbackChannel : ScanCallback() {
        val resultChannel = Channel<ScanResult>(Channel.BUFFERED)

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            resultChannel.offer(result)
        }

        override fun onScanFailed(errorCode: Int) {
            resultChannel.close(ScanFailedException(errorCode))
        }
    }
}

private val SCAN_TIMEOUT_MS = 8_000L