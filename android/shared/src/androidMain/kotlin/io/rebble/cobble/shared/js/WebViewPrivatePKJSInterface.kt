package io.rebble.cobble.shared.js

import android.net.Uri
import android.webkit.JavascriptInterface
import com.benasher44.uuid.Uuid
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.data.js.ActivePebbleWatchInfo
import io.rebble.cobble.shared.data.js.fromDevice
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

class WebViewPrivatePKJSInterface(private val jsRunner: WebViewJsRunner, private val scope: CoroutineScope, private val outgoingAppMessages: MutableSharedFlow<Pair<CompletableDeferred<UByte>, String>>): PrivatePKJSInterface, KoinComponent {
    private val lockerDao: LockerDao by inject()

    @JavascriptInterface
    override fun privateLog(message: String) {
        Logging.v("privateLog: $message")
    }

    @JavascriptInterface
    override fun logInterceptedSend() {
        Logging.v("logInterceptedSend")
    }

    @JavascriptInterface
    override fun logInterceptedRequest() {
        Logging.v("logInterceptedRequest")
    }

    @JavascriptInterface
    override fun getVersionCode(): Int {
        Logging.v("getVersionCode")
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    override fun logLocationRequest() {
        Logging.v("logLocationRequest")
    }

    @JavascriptInterface
    override fun getTimelineTokenAsync(): String {
        val uuid = Uuid.fromString(jsRunner.appInfo.uuid)
        jsRunner.scope.launch {
            var token: String? = null
            val entry = lockerDao.getEntryByUuid(uuid.toString())
            if (entry != null) {
                token = entry.entry.userToken
                if (entry.entry.local && token == null) {
                    Logging.d("App is local, getting sandbox timeline token")
                    token = JsTokenUtil.getSandboxTimelineToken(uuid)
                    if (token == null) {
                        Logging.w("Failed to get sandbox timeline token")
                    } else {
                        lockerDao.update(entry.entry.copy(userToken = token))
                    }
                }
            } else {
                Logging.e("App not found in locker")
            }
            if (token == null) {
                jsRunner.signalTimelineTokenFail(uuid.toString())
            } else {
                jsRunner.signalTimelineToken(uuid.toString(), token)
            }
        }
        return uuid.toString()
    }

    @JavascriptInterface
    fun startupScriptHasLoaded(url: String) {
        Logging.v("Startup script has loaded: $url")
        val uri = Uri.parse(url)
        val params = uri.getQueryParameter("params")
        scope.launch {
            jsRunner.loadAppJs(params)
        }
    }

    @JavascriptInterface
    fun privateFnLocalStorageWrite(key: String, value: String) {
        Logging.v("privateFnLocalStorageWrite")
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    fun privateFnLocalStorageRead(key: String): String {
        Logging.v("privateFnLocalStorageRead")
        TODO("Not yet implemented")
    }

    @JavascriptInterface
    fun privateFnLocalStorageReadAll(): String {
        Logging.v("privateFnLocalStorageReadAll")
        return "{}"
    }

    @JavascriptInterface
    fun privateFnLocalStorageReadAll_AtPreregistrationStage(baseUriReference: String): String {
        Logging.v("privateFnLocalStorageReadAll_AtPreregistrationStage")
        return privateFnLocalStorageReadAll()
    }

    @JavascriptInterface
    fun signalAppScriptLoadedByBootstrap() {
        Logging.v("signalAppScriptLoadedByBootstrap")
        scope.launch {
            jsRunner.signalReady()
        }
    }

    @JavascriptInterface
    fun sendAppMessageString(jsonAppMessage: String): Int {
        Logging.v("sendAppMessageString")
        val completable = CompletableDeferred<UByte>()
        if (!outgoingAppMessages.tryEmit(Pair(completable, jsonAppMessage))) {
            Logging.e("Failed to emit outgoing AppMessage")
            error("Failed to emit outgoing AppMessage")
        }
        return runBlocking {
            withTimeout(10.seconds) {
                completable.await().toInt()
            }
        }
    }

    @JavascriptInterface
    fun getActivePebbleWatchInfo(): String {
        return ConnectionStateManager.connectionState.value.watchOrNull?.let {
            Json.encodeToString(ActivePebbleWatchInfo.fromDevice(it))
        } ?: error("No active watch")
    }
}