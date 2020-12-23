package io.rebble.cobble.bridges.common

import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.pigeons.Pigeons
import timber.log.Timber
import javax.inject.Inject

class LoggingFlutterBridge @Inject constructor(
        bridgeLifecycleController: BridgeLifecycleController
) : Pigeons.PigeonLogger, FlutterBridge {
    init {
        bridgeLifecycleController.setupControl(Pigeons.PigeonLogger::setup, this)
    }

    override fun v(arg: Pigeons.StringWrapper) {
        Timber.v(arg.value)
    }

    override fun d(arg: Pigeons.StringWrapper) {
        Timber.d(arg.value)
    }

    override fun i(arg: Pigeons.StringWrapper) {
        Timber.i(arg.value)
    }

    override fun w(arg: Pigeons.StringWrapper) {
        Timber.w(arg.value)
    }

    override fun e(arg: Pigeons.StringWrapper) {
        Timber.e(arg.value)
    }
}