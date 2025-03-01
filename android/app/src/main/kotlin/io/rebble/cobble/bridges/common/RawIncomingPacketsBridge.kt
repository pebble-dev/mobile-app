package io.rebble.cobble.bridges.common

import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.datasources.IncomingPacketsListener
import io.rebble.cobble.pigeons.ListWrapper
import io.rebble.cobble.pigeons.Pigeons
import kotlinx.coroutines.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class RawIncomingPacketsBridge
    @Inject
    constructor(
        bridgeLifecycleController: BridgeLifecycleController,
        private val coroutineScope: CoroutineScope,
        private val incomingPacketsListener: IncomingPacketsListener
    ) : FlutterBridge, Pigeons.RawIncomingPacketsControl {
        private val connectionCallbacks =
            bridgeLifecycleController
                .createCallbacks(Pigeons::RawIncomingPacketsCallbacks)

        private var packetsObservingJob: Job? = null

        init {
            bridgeLifecycleController.setupControl(Pigeons.RawIncomingPacketsControl::setup, this)
        }

        override fun observeIncomingPackets() {
            packetsObservingJob =
                coroutineScope.launch(Dispatchers.Main.immediate) {
                    incomingPacketsListener.receivedPackets.collect {
                        val byteList = it.toList()

                        connectionCallbacks.onPacketReceived(ListWrapper(byteList)) {}
                    }
                }
        }

        override fun cancelObservingIncomingPackets() {
            packetsObservingJob?.cancel()
        }
    }