package io.rebble.cobble.bridges.ui

import android.content.Context
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.log.collectAndShareLogs
import io.rebble.cobble.pigeons.Pigeons
import javax.inject.Inject

class DebugFlutterBridge @Inject constructor(
        private val context: Context,
        bridgeLifecycleController: BridgeLifecycleController
) : FlutterBridge, Pigeons.DebugControl {
    init {
        bridgeLifecycleController.setupControl(Pigeons.DebugControl::setup, this)
    }

    override fun collectLogs(rwsId: String) {
        collectAndShareLogs(context, rwsId)
    }
}