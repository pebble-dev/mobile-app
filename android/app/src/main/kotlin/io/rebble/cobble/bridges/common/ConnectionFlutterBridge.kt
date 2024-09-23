package io.rebble.cobble.bridges.common

import io.rebble.cobble.bluetooth.ConnectionLooper
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.data.toPigeon
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import io.rebble.libpebblecommon.ProtocolHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class ConnectionFlutterBridge @Inject constructor(
        bridgeLifecycleController: BridgeLifecycleController,
        private val connectionLooper: ConnectionLooper,
        private val coroutineScope: CoroutineScope
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
        error("Deprecated")
    }

    override fun observeConnectionChanges() {
        statusObservingJob = coroutineScope.launch(Dispatchers.Main) {
            ConnectionStateManager.connectionState.map { connectionState ->
                val bluetoothDevice = connectionState.watchOrNull
                val watchMetadata = connectionState.watchOrNull?.metadata?.value
                val model = watchMetadata?.running?.hardwarePlatform?.get()?.toInt()
                Pigeons.WatchConnectionStatePigeon.Builder()
                        .setIsConnected(connectionState is ConnectionState.Connected ||
                                connectionState is ConnectionState.RecoveryMode)
                        .setIsConnecting(connectionState is ConnectionState.Connecting ||
                                connectionState is ConnectionState.WaitingForReconnect ||
                                connectionState is ConnectionState.WaitingForTransport ||
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