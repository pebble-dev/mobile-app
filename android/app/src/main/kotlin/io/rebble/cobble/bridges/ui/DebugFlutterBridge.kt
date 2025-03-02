package io.rebble.cobble.bridges.ui

import android.content.Context
import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.log.collectAndShareLogs
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.shared.datastore.KMPPrefs
import io.rebble.cobble.shared.errors.GlobalExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class DebugFlutterBridge
    @Inject
    constructor(
        private val context: Context,
        private val prefs: KMPPrefs,
        private val globalExceptionHandler: GlobalExceptionHandler,
        bridgeLifecycleController: BridgeLifecycleController
    ) : FlutterBridge, Pigeons.DebugControl {
        private val scope = CoroutineScope(Dispatchers.Main + globalExceptionHandler)

        init {
            bridgeLifecycleController.setupControl(Pigeons.DebugControl::setup, this)
        }

        override fun collectLogs(rwsId: String) {
            collectAndShareLogs(context, rwsId)
        }

        override fun getSensitiveLoggingEnabled(result: Pigeons.Result<Boolean>) {
            scope.launch {
                result.success(prefs.sensitiveDataLoggingEnabled.first())
            }
        }

        override fun setSensitiveLoggingEnabled(
            enabled: Boolean,
            result: Pigeons.Result<Void>
        ) {
            scope.launch {
                prefs.setSensitiveDataLoggingEnabled(enabled)
                result.success(null)
            }
        }
    }