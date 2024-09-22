package io.rebble.cobble.shared.js

import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.PlatformContext
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.handlers.getAppPbwFile
import io.rebble.cobble.shared.util.getPbwJsFilePath
import io.rebble.cobble.shared.util.requirePbwAppInfo
import io.rebble.cobble.shared.util.requirePbwJsFilePath
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import okio.buffer
import okio.use
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PKJSApp(val uuid: Uuid): KoinComponent {
    private val context: PlatformContext by inject()
    private val pbw = getAppPbwFile(context, uuid.toString())
    private val appInfo = requirePbwAppInfo(pbw)
    private val jsPath = requirePbwJsFilePath(context, appInfo, pbw)
    private var jsRunner: JsRunner? = null

    companion object {
        fun isJsApp(context: PlatformContext, uuid: Uuid): Boolean {
            val pbw = getAppPbwFile(context, uuid.toString())
            return pbw.exists() && getPbwJsFilePath(context, requirePbwAppInfo(pbw), pbw) != null
        }
    }

    suspend fun start(device: PebbleDevice) {
        withTimeout(1000) {
            val connectionScope = device.connectionScope.filterNotNull().first()
            jsRunner = JsRunnerFactory.createJsRunner(connectionScope, device.address, appInfo, jsPath)
        }
        jsRunner?.start()
    }

    suspend fun stop() {
        jsRunner?.stop()
        jsRunner = null
    }
}