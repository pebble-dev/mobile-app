package io.rebble.fossil.handlers

import io.rebble.libpebblecommon.services.SystemService
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
class SystemHandler @Inject constructor(
        private val coroutineScope: CoroutineScope,
        private val systemService: SystemService
) {
    init {
        listenForIncomingPackets()
    }

    private fun listenForIncomingPackets() {
        // Nothing yet
    }
}