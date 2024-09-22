package io.rebble.cobble.shared.js

import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent

actual object JsRunnerFactory: KoinComponent {
    actual fun createJsRunner(scope: CoroutineScope, connectedAddress: String, appInfo: PbwAppInfo, jsPath: String): JsRunner = TODO()
}