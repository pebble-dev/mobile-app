package io.rebble.fossil.bluetooth.scan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import io.rebble.fossil.bluetooth.BluePebbleDevice
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class BleScanner @Inject constructor() {
    private val _isScanning = MutableStateFlow<Boolean>(false)
    val isScanning: StateFlow<Boolean> get() = _isScanning

    private var stopTrigger: CompletableDeferred<Unit>? = null

    fun getScanFlow(): Flow<List<BluePebbleDevice>> = flow {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                ?: throw BluetoothNotSupportedException("Device does not have a bluetooth adapter")

        val leScanner = bluetoothAdapter.bluetoothLeScanner
        val callback = FlowScanCallback()

        var foundDevices = emptyList<BluePebbleDevice>()

        val timeoutFlow = flow<Boolean> {
            emit(false)
            delay(SCAN_TIMEOUT_MS)
            emit(true)
        }

        try {
            leScanner.startScan(callback)

            val leFlow = callback.asFlow()

            leFlow
                    .combine(timeoutFlow) { result, timeoutReached -> Pair(result, timeoutReached) }
                    .collect { (result, timeoutReached) ->
                        if (timeoutReached) {
                            callback.close()
                            return@collect
                        }

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
        } finally {
            leScanner.stopScan(callback)
        }
    }


    fun stopScan() {
        stopTrigger?.complete(Unit)
    }

    private class FlowScanCallback : ScanCallback() {
        private val resultChannel = Channel<ScanResult>(Channel.BUFFERED)

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            resultChannel.offer(result)
        }

        override fun onScanFailed(errorCode: Int) {
            resultChannel.close(ScanFailedException(errorCode))
        }

        fun close() {
            resultChannel.close()
        }

        fun asFlow(): Flow<ScanResult> {
            return resultChannel.receiveAsFlow()
        }
    }
}

private val SCAN_TIMEOUT_MS = 8_000L