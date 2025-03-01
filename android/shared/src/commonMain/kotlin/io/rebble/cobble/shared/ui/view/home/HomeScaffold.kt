package io.rebble.cobble.shared.ui.view.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.nav.Routes
import io.rebble.cobble.shared.ui.view.home.locker.Locker
import io.rebble.cobble.shared.ui.view.home.locker.LockerTabs
import io.rebble.cobble.shared.ui.view.home.watches.WatchesPage
import kotlinx.coroutines.launch

open class HomePage {
    class Locker(val tab: LockerTabs) : HomePage()
    object TestPage : HomePage()
    object WatchesPage : HomePage()
}

@Composable
fun HomeScaffold(page: HomePage, onNavChange: (String) -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val searchingState = remember { mutableStateOf(false) }
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp), // Needed in scaffold for edgetoedge to work
        snackbarHost = { SnackbarHost(snackbarHostState) },
        /*topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = { Text("Cobble") },
            )
        },*/
        bottomBar = {
            NavigationBar(
                    windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = page is HomePage.TestPage,
                    onClick = { onNavChange(Routes.Home.TEST_PAGE) },
                    icon = { RebbleIcons.settings() },
                    label = { Text("Test") }
                )
                NavigationBarItem(
                    selected = page is HomePage.Locker,
                    onClick = { onNavChange(Routes.Home.LOCKER_WATCHFACES) },
                    icon = { RebbleIcons.locker() },
                    label = { Text("Locker") }
                )

                NavigationBarItem(
                    selected = page is HomePage.WatchesPage,
                    onClick = { onNavChange(Routes.Home.WATCHES_PAGE) },
                    icon = { RebbleIcons.devices() },
                    label = { Text("Devices") }
                )
            }
        },
        floatingActionButton = {
            when (page) {
                is HomePage.Locker -> {
                    FloatingActionButton(
                            modifier = Modifier
                                    .padding(16.dp),
                            onClick = {
                                searchingState.value = true
                            },
                            content = {
                                RebbleIcons.search()
                            },
                    )
                }
                is HomePage.WatchesPage -> {
                    FloatingActionButton(
                            modifier = Modifier
                                    .padding(16.dp),
                            onClick = {
                                searchingState.value = false //TODO Change this so that it actually goes into pairing mode.
                            },
                            content = {
                                Icon(Icons.Filled.Add, "Pair a watch")
                            }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (page) {
                is HomePage.Locker -> {
                    Locker(searchingState, page.tab, onTabChanged = {
                        onNavChange(it.navRoute)
                    })
                }
                is HomePage.TestPage -> {
                    TestPage(onShowSnackbar = {
                        scope.launch {
                            snackbarHostState.showSnackbar(message = it)
                        }
                    })
                }
                is HomePage.WatchesPage -> {
                    WatchesPage()
                }
            }
        }
    }
}