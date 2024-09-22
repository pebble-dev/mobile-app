package io.rebble.cobble.shared.js

import android.content.Context
import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual object JsRunnerFactory: KoinComponent {
    private val context: Context by inject()
    actual fun createJsRunner(
            scope: CoroutineScope,
            connectedAddress: String,
            appInfo: PbwAppInfo,
            jsPath: String
    ): JsRunner = WebViewJsRunner(context, connectedAddress, scope, appInfo, jsPath)
}