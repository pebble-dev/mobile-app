package io.rebble.cobble.bridges.common

import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bluetooth.ConnectionState
import io.rebble.cobble.bluetooth.watchOrNull
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.data.toPigeon
import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.cobble.pigeons.BooleanWrapper
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.util.macAddressToLong
import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
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
        return BooleanWrapper(connectionLooper.connectionState.value is ConnectionState.Connected)
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
                Pigeons.WatchConnectionStatePigeon().apply {
                    isConnected = connectionState is ConnectionState.Connected
                    isConnecting = connectionState is ConnectionState.Connecting ||
                            connectionState is ConnectionState.WaitingForReconnect
                    val bluetoothDevice = connectionState.watchOrNull
                    currentWatchAddress = bluetoothDevice?.address?.macAddressToLong()
                    currentConnectedWatch = watchMetadata.toPigeon(bluetoothDevice, model)
                }
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