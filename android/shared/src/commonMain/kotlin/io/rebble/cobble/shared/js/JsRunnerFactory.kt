package io.rebble.cobble.shared.js

import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent

expect object JsRunnerFactory: KoinComponent {
    fun createJsRunner(scope: CoroutineScope, appInfo: PbwAppInfo, jsPath: String): JsRunner
}