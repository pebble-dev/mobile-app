package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothGattService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface GattService {
    /**
     * Called by a GATT server to register the service.
     * Starts consuming events from the [eventFlow] (usually a [SharedFlow]) and handles them.
     */
    fun register(eventFlow: Flow<ServerEvent>): BluetoothGattService
}