package io.rebble.cobble.bridges.ui

import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.datasources.AndroidPreferences
import io.rebble.cobble.pigeons.Pigeons
import javax.inject.Inject

class BackgroundSetupFlutterBridge @Inject constructor(
        bridgeLifecycleController: BridgeLifecycleController,
        private val androidPreferences: AndroidPreferences
) : FlutterBridge, Pigeons.BackgroundSetupControl {
    init {
        bridgeLifecycleController.setupControl(Pigeons.BackgroundSetupControl::setup, this)
    }

    override fun setupBackground(arg: Pigeons.NumberWrapper) {
        androidPreferences.backgroundEndpoint = arg.value
    }

}