package io.rebble.fossil.handlers

import io.rebble.fossil.di.PerService
import io.rebble.libpebblecommon.services.SystemService
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalStdlibApi::class)
@PerService
class SystemHandler @Inject constructor(
        private val coroutineScope: CoroutineScope,
        private val systemService: SystemService
) : PebbleMessageHandler {
    init {
        listenForIncomingPackets()
    }

    private fun listenForIncomingPackets() {
        // Nothing yet
    }
}