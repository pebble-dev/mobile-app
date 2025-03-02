package io.rebble.cobble.shared.js

import android.content.Context
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual object JsRunnerFactory : KoinComponent {
    private val context: Context by inject()

    actual fun createJsRunner(
        scope: CoroutineScope,
        device: PebbleDevice,
        appInfo: PbwAppInfo,
        jsPath: String
    ): JsRunner = WebViewJsRunner(context, device, scope, appInfo, jsPath)
}