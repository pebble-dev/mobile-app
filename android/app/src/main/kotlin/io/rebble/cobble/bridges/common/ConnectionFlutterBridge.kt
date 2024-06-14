package io.rebble.cobble.bridges.common

import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import io.rebble.cobble.bluetooth.watchOrNull
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.data.toPigeon
import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionFlutterBridge @Inject constructor(
        bridgeLifecycleController: BridgeLifecycleController,
        private val connectionLooper: ConnectionLooper,
        private val coroutineScope: CoroutineScope,
        private val protocolHandler: ProtocolHandler,
        private val watchMetadataStore: WatchMetadataStore
) : FlutterBridge, Pigeons.ConnectionControl {
    private val connectionCallbacks = bridgeLifecycleController
            .createCallbacks(Pigeons::ConnectionCallbacks)

    private var statusObservingJob: Job? = null

    init {
        bridgeLifecycleController.setupControl(Pigeons.ConnectionControl::setup, this)
    }

    override fun isConnected(): Pigeons.BooleanWrapper {
        return Pigeons.BooleanWrapper().apply {
            value = connectionLooper.connectionState.value is ConnectionState.Connected
        }
    }


    override fun disconnect() {
        connectionLooper.closeConnection()
    }

    @Suppress("UNCHECKED_CAST")
    override fun sendRawPacket(arg: Pigeons.ListWrapper) {
        coroutineScope.launch {
            val byteArray = (arg.value as List<Number>).map { it.toByte().toUByte() }.toUByteArray()
            protocolHandler.send(byteArray)
        }
    }

    override fun observeConnectionChanges() {
        statusObservingJob = coroutineScope.launch(Dispatchers.Main) {
            combine(
                    connectionLooper.connectionState,
                    watchMetadataStore.lastConnectedWatchMetadata,
                    watchMetadataStore.lastConnectedWatchModel
            ) { connectionState, watchMetadata, model ->
                val bluetoothDevice = connectionState.watchOrNull
                Pigeons.WatchConnectionStatePigeon.Builder()
                        .setIsConnected(connectionState is ConnectionState.Connected ||
                                connectionState is ConnectionState.RecoveryMode)
                        .setIsConnecting(connectionState is ConnectionState.Connecting ||
                                connectionState is ConnectionState.WaitingForReconnect ||
                                connectionState is ConnectionState.WaitingForBluetoothToEnable ||
                                connectionState is ConnectionState.Negotiating)
                        .setCurrentWatchAddress(bluetoothDevice?.address)
                        .setCurrentConnectedWatch(watchMetadata.toPigeon(bluetoothDevice, model))
                        .build()
            }.collect {
                connectionCallbacks.onWatchConnectionStateChanged(
                        it
                ) {}
            }
        }
    }

    override fun cancelObservingConnectionChanges() {
        statusObservingJob?.cancel()
    }
}