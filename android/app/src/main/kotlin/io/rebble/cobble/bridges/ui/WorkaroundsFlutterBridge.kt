package io.rebble.cobble.bridges.ui

import android.content.Context
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.pigeons.ListWrapper
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.shared.workarounds.WorkaroundDescriptor
import javax.inject.Inject

class WorkaroundsFlutterBridge @Inject constructor(
        private val context: Context,
        lifecycleController: BridgeLifecycleController
) : FlutterBridge, Pigeons.WorkaroundsControl {
    init {
        lifecycleController.setupControl(Pigeons.WorkaroundsControl::setup, this)
    }

    override fun getNeededWorkarounds(): Pigeons.ListWrapper {
        return ListWrapper(
                WorkaroundDescriptor.allWorkarounds
                        .filter { it.isNeeded(context) }
                        .map { it.name }
        )
    }
}