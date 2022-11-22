package io.rebble.cobble.bluetooth.gatt

import io.rebble.libpebblecommon.ble.GATTPacket
import kotlinx.coroutines.flow.Flow

interface PPoGATTServer {
    val currentMtu: Flow<Int>
    val packetsFromWatch: Flow<GATTPacket>
    suspend fun sendToWatch(packet: GATTPacket)
}