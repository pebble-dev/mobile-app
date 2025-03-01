package io.rebble.cobble.bluetooth.scan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.annotation.RequiresPermission
import io.rebble.cobble.bluetooth.ScannedPebbleDevice
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import javax.inject.Inject
import javax.inject.Singleton

private val SCAN_TIMEOUT_MS = 8_000L

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class BleScanner
    @Inject
    constructor() {
        private var stopTrigger: CompletableDeferred<Unit>? = null

        @RequiresPermission(
            allOf = [android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT]
        )
        fun getScanFlow(): Flow<List<ScannedPebbleDevice>> =
            flow {
                coroutineScope {
                    val bluetoothAdapter =
                        BluetoothAdapter.getDefaultAdapter()
                            ?: throw BluetoothNotSupportedException("Device does not have a bluetooth adapter")

                    val leScanner = bluetoothAdapter.bluetoothLeScanner
                    val callback = ScanCallbackChannel()

                    var foundDevices = emptyList<ScannedPebbleDevice>()

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
                                        (device.type == BluetoothDevice.DEVICE_TYPE_LE || device.type == BluetoothDevice.DEVICE_TYPE_DUAL) &&
                                        (
                                            device.name.startsWith("Pebble ") ||
                                                device.name.startsWith("Pebble-LE")
                                        )
                                    ) {
                                        val i = foundDevices.indexOfFirst { it.bluetoothDevice.address == device.address }
                                        if (i < 0) {
                                            val scannedPebbleDevice = ScannedPebbleDevice(result)
                                            foundDevices = foundDevices + scannedPebbleDevice
                                            emit(foundDevices)
                                        } else if (foundDevices[i].leMeta?.color == null) {
                                            val fd = foundDevices as MutableList
                                            fd[i] = ScannedPebbleDevice(result)
                                            foundDevices = fd
                                            emit(foundDevices)
                                        }
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
                        leScanner.stopScan(callback)
                        // callback.resultChannel.cancel()
                        this@BleScanner.stopTrigger = null
                    }
                }
            }

        fun stopScan() {
            stopTrigger?.complete(Unit)
        }

        private class ScanCallbackChannel : ScanCallback() {
            val resultChannel = Channel<ScanResult>(Channel.BUFFERED)

            override fun onScanResult(
                callbackType: Int,
                result: ScanResult
            ) {
                resultChannel.trySend(result).isSuccess
            }

            override fun onScanFailed(errorCode: Int) {
                resultChannel.close(ScanFailedException(errorCode))
            }
        }
    }