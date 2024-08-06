package io.rebble.cobble.shared.js

import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo

abstract class JsRunner(val appInfo: PbwAppInfo, val jsPath: String) {
    abstract suspend fun start()
    abstract suspend fun stop()
    abstract fun loadUrl(url: String)
}