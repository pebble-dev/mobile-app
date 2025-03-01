package io.rebble.cobble.shared.js

import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent

expect object JsRunnerFactory : KoinComponent {
    fun createJsRunner(
        scope: CoroutineScope,
        device: PebbleDevice,
        appInfo: PbwAppInfo,
        jsPath: String
    ): JsRunner
}