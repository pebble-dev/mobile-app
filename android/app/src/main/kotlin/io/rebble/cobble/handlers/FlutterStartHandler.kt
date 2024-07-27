package io.rebble.cobble.handlers

import io.rebble.cobble.bridges.background.FlutterBackgroundController
import io.rebble.cobble.shared.handlers.CobbleHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handler that (re)starts dart background environment every time service is restarted
 */
class FlutterStartHandler @Inject constructor(
        coroutineScope: CoroutineScope,
        backgroundController: FlutterBackgroundController
) : CobbleHandler {
    init {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            backgroundController.getBackgroundFlutterEngine()
        }
    }
}