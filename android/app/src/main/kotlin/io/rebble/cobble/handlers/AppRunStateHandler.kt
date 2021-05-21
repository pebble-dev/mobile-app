package io.rebble.cobble.handlers

import io.rebble.cobble.datasources.WatchMetadataStore
import io.rebble.libpebblecommon.packets.AppRunStateMessage
import io.rebble.libpebblecommon.services.app.AppRunStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppRunStateHandler @Inject constructor(
        coroutineScope: CoroutineScope,
        private val appRunStateService: AppRunStateService,
        private val watchMetadataStore: WatchMetadataStore
) : CobbleHandler {
    init {
        coroutineScope.launch { listenForAppStateChanges() }
    }

    private suspend fun listenForAppStateChanges() {
        try {
            for (message in appRunStateService.receivedMessages) {
                when (message) {
                    is AppRunStateMessage.AppRunStateStart -> {
                        watchMetadataStore.currentActiveApp.value = message.uuid.get()
                    }
                    is AppRunStateMessage.AppRunStateStop -> {
                        watchMetadataStore.currentActiveApp.value = null
                    }
                    is AppRunStateMessage.AppRunStateRequest -> {
                        // Not supported
                    }
                }
            }
        } finally {
            watchMetadataStore.currentActiveApp.value = null
        }
    }
}