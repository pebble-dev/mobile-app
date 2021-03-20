package io.rebble.cobble.bridges.common

import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.pigeons.BooleanWrapper
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.util.launchPigeonResult
import io.rebble.libpebblecommon.packets.AppRunStateMessage
import io.rebble.libpebblecommon.services.app.AppRunStateService
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class AppLifecycleFlutterBridge @Inject constructor(
        bridgeLifecycleController: BridgeLifecycleController,
        private val coroutineScope: CoroutineScope,
        private val appRunStateService: AppRunStateService
) : Pigeons.AppLifecycleControl, FlutterBridge {
    init {
        bridgeLifecycleController.setupControl(Pigeons.AppLifecycleControl::setup, this)
    }

    override fun openAppOnTheWatch(
            uuidString: Pigeons.StringWrapper,
            result: Pigeons.Result<Pigeons.BooleanWrapper>
    ) {
        coroutineScope.launchPigeonResult(result) {
            appRunStateService.send(AppRunStateMessage.AppRunStateStart(
                    UUID.fromString(uuidString.value))
            )

            BooleanWrapper(true)
        }
    }
}