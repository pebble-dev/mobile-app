package io.rebble.cobble.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.multiplatform.webview.cookie.Cookie
import com.multiplatform.webview.request.RequestInterceptor
import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.web.WebViewNavigator
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.parametersOf
import io.rebble.cobble.shared.api.RWS
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.CurrentToken
import io.rebble.cobble.shared.domain.store.*
import io.rebble.cobble.shared.ui.view.home.store.StoreTabs
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class StoreViewModel : ViewModel() {
    val watchIsConnected = ConnectionStateManager.isConnected
    val watchConnectionState = ConnectionStateManager.connectionState
    val searchQuery = MutableStateFlow<String?>(null)
    val resultFlow = MutableSharedFlow<LockerResult>()

    private val _searchingState = MutableStateFlow(false)
    val searchingState = _searchingState.asStateFlow()

    private val _searchButton = MutableStateFlow(false)
    val searchButton = _searchButton.asStateFlow()

    private val _expandedDropdown = MutableStateFlow(false)
    val expandedDropdown = _expandedDropdown.asStateFlow()

    private val _initialUrl = MutableStateFlow<String?>(null)
    val initialUrl = _initialUrl.asStateFlow()

    // Stores current page title from WebView
    private val _pageTitle = MutableStateFlow<String?>("Rebble store")
    val pageTitle = _pageTitle.asStateFlow()

    // Store current tab
    private val _currentTab = MutableStateFlow<StoreTabs>(StoreTabs.Watchfaces)
    val currentTab = _currentTab.asStateFlow()

    val openUrlEvent = MutableSharedFlow<String>()

    sealed class LockerResult(
        open val callbackId: Int
    ) {
        data class Success(
            override val callbackId: Int
        ) : LockerResult(callbackId)

        data class Failure(
            val errorMessage: String,
            override val callbackId: Int
        ) : LockerResult(callbackId)
    }

    fun handleSetNavBarTitle(paramData: JsFrame<SetNavBarTitle>) {
        _pageTitle.value = paramData.data.browserTitle ?: paramData.data.title
        _searchButton.value = paramData.data.showSearch
    }

    fun handleOpenURL(paramData: JsFrame<OpenURL>) {
        viewModelScope.launch {
            openUrlEvent.emit(paramData.data.url)
        }
    }

    fun performSearch(navigator: WebViewNavigator) {
        println("hello?")
        val jsRequest = PebbleBridge.navigateRequest(url = baseUrl(search = searchQuery.value))
        println(jsRequest)
        navigator.evaluateJavaScript(jsRequest)
        _searchingState.value = false
    }

    fun saveItemToLocker(
        uuid: String,
        callbackId: Int
    ) {
        viewModelScope.launch {
            RWS.appstoreClientFlow.collect { appstoreClient ->
                if (appstoreClient != null) {
                    val result =
                        try {
                            appstoreClient.addToLocker(uuid)
                            LockerResult.Success(callbackId)
                        } catch (e: Exception) {
                            LockerResult.Failure(
                                errorMessage = "Failed to add item: ${e.localizedMessage}",
                                callbackId
                            )
                        }
                    resultFlow.emit(result)
                } else {
                    resultFlow.emit(
                        LockerResult.Failure(
                            errorMessage = "AppstoreClient is not available",
                            callbackId
                        )
                    )
                }
            }
        }
    }

    fun setSearching(searching: Boolean) {
        _searchingState.value = searching
        if (!searching) {
            searchQuery.value = null
        }
    }

    fun toggleDropdown() {
        _expandedDropdown.value = !_expandedDropdown.value
    }

    fun closeDropdown() {
        _expandedDropdown.value = false
    }

    fun setCurrentTab(tab: StoreTabs) {
        _currentTab.value = tab
        generateInitialUrl()
    }

    fun generateInitialUrl() {
        _initialUrl.value = baseUrl()
    }

    fun baseUrl(search: String? = null): String {
        var url = "https://apps.rebble.io/en_US/" // TODO: Use an actual locale

        if (search != null) {
            url += "search/"
        }
        url +=
            when (_currentTab.value) {
                StoreTabs.Watchfaces -> "watchfaces/"
                StoreTabs.Apps -> "watchapps/"
            }

        if (search != null) {
            url += "1?query=$search"
        }

        url =
            URLBuilder(url)
                .apply {
                    parameters.appendAll(
                        parametersOf(
                            "native" to listOf("true"),
                            "inApp" to listOf("true"),
                            "platform" to listOf("android"),
                            "app_version" to listOf("0.0.1"),
                            "release_id" to listOf("100")
                        )
                    )
                }.buildString()

        val currentState = watchConnectionState.value
        if (currentState is ConnectionState.Connected) {
            val watchDevice = currentState.watch
            val metadata = watchDevice.metadata.value!!

            val pebbleColor = watchDevice.modelId.value
            val pebbleHardware =
                WatchHardwarePlatform
                    .fromProtocolNumber(
                        metadata.running.hardwarePlatform.get()
                    )?.watchType
                    ?.codename
            val pebbleId = metadata.serial.get()

            url =
                URLBuilder(url)
                    .apply {
                        parameters.appendAll(
                            parametersOf(
                                "pebble_color" to listOf(pebbleColor.toString()),
                                "hardware" to listOf(pebbleHardware ?: ""),
                                "pid" to listOf(pebbleId)
                            )
                        )
                    }.buildString()
        }
        return url
    }

    // Used to initialize cookies for the WebView
    suspend fun prepareTokenCookie(): Cookie? {
        var cookie: Cookie? = null
        // Collect the current token state from the flow
        RWS.currentTokenFlow.collect { tokenState ->
            when (tokenState) {
                is CurrentToken.LoggedIn -> {
                    cookie =
                        Cookie(
                            name = "access_token",
                            value = tokenState.token,
                            domain = "apps.rebble.io"
                        )
                    return@collect // Break out of collection after getting value
                }
                else -> {}
            }
        }
        return cookie
    }

    // Create a request interceptor for WebView
    fun createRequestInterceptor(): RequestInterceptor {
        return object : RequestInterceptor {
            override fun onInterceptUrlRequest(
                request: WebRequest,
                navigator: WebViewNavigator
            ): WebRequestInterceptResult {
                println(request.url)
                if (request.url.startsWith("pebble-method-call-js-frame")) {
                    val fullUrl = request.url.replaceRange(30, 30, "?")
                    val url = Url(fullUrl)
                    val parametersArgs = url.parameters.get("args")!!
                    println(parametersArgs)
                    when (url.parameters.get("method")) {
                        "setNavBarTitle" -> {
                            val paramData =
                                Json.decodeFromString<JsFrame<SetNavBarTitle>>(
                                    parametersArgs
                                )
                            handleSetNavBarTitle(paramData)
                        }
                        "openURL" -> {
                            val paramData = Json.decodeFromString<JsFrame<OpenURL>>(parametersArgs)
                            handleOpenURL(paramData)
                        }
                        "loadAppToDeviceAndLocker" -> {
                            val paramData =
                                Json
                                    .decodeFromString<JsFrame<LoadAppToDeviceAndLocker>>(
                                        parametersArgs
                                    )
                            saveItemToLocker(
                                uuid = paramData.data.uuid,
                                callbackId = paramData.callbackId
                            )
                        }
                        /*
                        // TODO:
                        "setVisibleApp" -> {
                          val paramData = Json.decodeFromString<JsFrame<SetVisibleApp>>(parametersArgs)
                          println(paramData)
                        }
                         */
                    }
                    return WebRequestInterceptResult.Reject
                }

                return WebRequestInterceptResult.Allow
            }
        }
    }

    init {
        // Generate initial URL when VM is created
        generateInitialUrl()
    }
}