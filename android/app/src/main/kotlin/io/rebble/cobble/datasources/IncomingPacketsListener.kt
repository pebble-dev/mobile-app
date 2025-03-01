package io.rebble.cobble.datasources

import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomingPacketsListener
    @Inject
    constructor() {
        val receivedPackets = MutableSharedFlow<ByteArray>()
    }