package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MockGattServer(val serverFlow: MutableSharedFlow<ServerEvent>, val scope: CoroutineScope): GattServer {
    val mockServerNotifies = Channel<GattServerImpl.ServerAction.NotifyCharacteristicChanged>(Channel.BUFFERED)

    private val mockServer: BluetoothGattServer = mockk()

    override fun getServer(): BluetoothGattServer {
        return mockServer
    }

    override fun getFlow(): Flow<ServerEvent> {
        return serverFlow
    }

    override suspend fun notifyCharacteristicChanged(device: BluetoothDevice, characteristic: BluetoothGattCharacteristic, confirm: Boolean, value: ByteArray) {
        scope.launch {
            mockServerNotifies.send(GattServerImpl.ServerAction.NotifyCharacteristicChanged(device, characteristic, confirm, value))
            serverFlow.emit(NotificationSentEvent(device, BluetoothGatt.GATT_SUCCESS))
        }
    }
}