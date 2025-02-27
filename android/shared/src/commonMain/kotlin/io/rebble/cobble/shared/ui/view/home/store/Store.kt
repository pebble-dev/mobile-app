package io.rebble.cobble.shared.ui.view.home.store

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.rebble.cobble.shared.api.RWS
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.CurrentToken
import io.rebble.cobble.shared.domain.store.*
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.nav.Routes
import io.rebble.cobble.shared.ui.viewmodel.StoreViewModel
import com.multiplatform.webview.cookie.Cookie
import com.multiplatform.webview.jsbridge.rememberWebViewJsBridge
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import kotlinx.coroutines.flow.collectLatest

enum class StoreTabs(val label: String, val navRoute: String) {
    Watchfaces("Watchfaces", Routes.Home.STORE_WATCHFACES),
    Apps("Apps", Routes.Home.STORE_APPS),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Store(page: StoreTabs, viewModel: StoreViewModel = viewModel { StoreViewModel() }, onTabChanged: (StoreTabs) -> Unit) {
    LaunchedEffect(page) {
        viewModel.setCurrentTab(page)
    }

    val watchIsConnected by viewModel.watchIsConnected.collectAsState()
    val watchConnectionState by viewModel.watchConnectionState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searching by viewModel.searchingState.collectAsState(initial = false)
    val searchButton by viewModel.searchButton.collectAsState(initial = true)
    val expanded by viewModel.expandedDropdown.collectAsState(initial = false)
    val initialUrl by viewModel.initialUrl.collectAsState(initial = null)
    val pageTitle by viewModel.pageTitle.collectAsState(initial = "Rebble Store")

    val tokenState by RWS.currentTokenFlow.collectAsState(initial = CurrentToken.LoggedOut)

    val focusRequester = remember { FocusRequester() }

    if (initialUrl == null || watchConnectionState !is ConnectionState.Connected) {
        return
    }

    val state = rememberWebViewState(url = initialUrl!!)
    val jsBridge = rememberWebViewJsBridge()
    val navigator = rememberWebViewNavigator(
        requestInterceptor = viewModel.createRequestInterceptor()
    )

    val uriHandler = LocalUriHandler.current

    LaunchedEffect(tokenState) {
        when (tokenState) {
            is CurrentToken.LoggedIn -> {
                val token = (tokenState as CurrentToken.LoggedIn).token
                state.cookieManager.removeAllCookies()
                state.cookieManager.setCookie(
                    "https://apps.rebble.io",
                    Cookie(
                        name = "access_token",
                        value = token,
                        domain = "apps.rebble.io"
                    )
                )
            }
            else -> {
                state.cookieManager.removeAllCookies()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.resultFlow.collectLatest { result ->
            val addedToLocker = result is StoreViewModel.LockerResult.Success
            val jsResponse = PebbleBridge.lockerResponse(callbackId = result.callbackId, addedToLocker)
            navigator.evaluateJavaScript(jsResponse)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.openUrlEvent.collect { url ->
            uriHandler.openUri(url)
        }
    }

    Column {
        if (searching) {
            SearchBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                query = searchQuery ?: "",
                onQueryChange = { newQuery -> viewModel.searchQuery.value = newQuery },
                onSearch = { viewModel.performSearch(navigator) },
                active = false,
                onActiveChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .focusRequester(focusRequester)
                    .onGloballyPositioned {
                        focusRequester.requestFocus()
                    },
                leadingIcon = {
                    IconButton(
                        onClick = { viewModel.setSearching(false) },
                        content = {
                            RebbleIcons.xClose()
                        }
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.performSearch(navigator) }
                    ) {
                        RebbleIcons.caretRight()
                    }
                },
                content = {}
            )
        } else {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        println("TopAppBar height: ${coordinates.size.height}")
                    },
                navigationIcon =  {
                    IconButton(
                        onClick = { navigator.navigateBack() },
                        enabled = navigator.canGoBack,
                        content = { RebbleIcons.caretLeft() }
                    )
                },

                title = {
                    TextButton(onClick = { viewModel.toggleDropdown() }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                pageTitle ?: "Rebble Store",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(page.label)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown"
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { viewModel.closeDropdown() }
                    ) {
                        StoreTabs.entries.forEach { tab ->
                            DropdownMenuItem(
                                text = { Text(tab.label) },
                                onClick = {
                                    onTabChanged(tab)
                                    viewModel.closeDropdown()
                                }
                            )
                        }
                    }
                },

                actions = {
                    if (searchButton) {
                        IconButton(
                            onClick = { viewModel.setSearching(true) },
                            content = { RebbleIcons.search() }
                        )
                    }
                }
            )
        }

        WebView(
            state = state,
            modifier = Modifier.fillMaxSize(),
            navigator = navigator,
            webViewJsBridge = jsBridge
        )
    }
}