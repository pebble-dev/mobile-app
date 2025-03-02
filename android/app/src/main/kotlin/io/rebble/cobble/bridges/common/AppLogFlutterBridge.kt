package io.rebble.cobble.bridges.common

import io.rebble.cobble.bridges.FlutterBridge
import io.rebble.cobble.bridges.ui.BridgeLifecycleController
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import io.rebble.cobble.shared.middleware.AppLogController
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AppLogFlutterBridge
    @Inject
    constructor(
        bridgeLifecycleController: BridgeLifecycleController
    ) : FlutterBridge, Pigeons.AppLogControl {
        private val callbacks = bridgeLifecycleController.createCallbacks(Pigeons::AppLogCallbacks)

        private var logsJob: Job? = null

        init {
            bridgeLifecycleController.setupControl(Pigeons.AppLogControl::setup, this)
        }

        override fun startSendingLogs() {
            stopSendingLogs()
            val pebbleDevice =
                ConnectionStateManager.connectionState.value.watchOrNull
                    ?: run {
                        Timber.e("No app log service available")
                        return
                    }
            pebbleDevice.negotiationScope.launch {
                val connectionScope = pebbleDevice.connectionScope.filterNotNull().first()
                logsJob =
                    connectionScope.launch {
                        val appLogController = AppLogController(pebbleDevice)
                        appLogController.logs.collect {
                            Timber.d("Received in pigeon '%s'", it.message.get())
                            callbacks.onLogReceived(
                                Pigeons.AppLogEntry.Builder()
                                    .setUuid(it.uuid.get().toString())
                                    .setTimestamp(it.timestamp.get().toLong())
                                    .setLevel(it.level.get().toLong())
                                    .setLineNumber(it.lineNumber.get().toLong())
                                    .setFilename(it.filename.get())
                                    .setMessage(it.message.get())
                                    .build()
                            ) {}
                        }
                    }
            }
        }

        override fun stopSendingLogs() {
            logsJob?.cancel()
        }
    }