package io.rebble.cobble.shared.js

import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

abstract class JsRunner(val appInfo: PbwAppInfo, val jsPath: String, val device: PebbleDevice) {
    abstract suspend fun start()
    abstract suspend fun stop()
    abstract fun loadUrl(url: String)
    abstract suspend fun signalNewAppMessageData(data: String?): Boolean
    abstract suspend fun signalAppMessageAck(data: String?): Boolean
    abstract suspend fun signalAppMessageNack(data: String?): Boolean

    protected val _outgoingAppMessages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val outgoingAppMessages = _outgoingAppMessages.asSharedFlow()
}