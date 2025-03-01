package io.rebble.cobble.shared.ui.view.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.nav.Routes
import io.rebble.cobble.shared.ui.view.home.locker.Locker
import io.rebble.cobble.shared.ui.view.home.locker.LockerTabs
import io.rebble.cobble.shared.ui.view.home.store.Store
import io.rebble.cobble.shared.ui.view.home.store.StoreTabs
import kotlinx.coroutines.launch

open class HomePage {
    class Locker(val tab: LockerTabs) : HomePage()

    class Store(val tab: StoreTabs) : HomePage()

    object TestPage : HomePage()
}

@Composable
fun HomeScaffold(
    page: HomePage,
    onNavChange: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
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
                    selected = page is HomePage.Store,
                    onClick = { onNavChange(Routes.Home.STORE_WATCHFACES) },
                    icon = { RebbleIcons.rebbleStore() },
                    label = { Text("Store") }
                )
            }
        },
        floatingActionButton = {
            when (page) {
                is HomePage.Locker -> {
                    FloatingActionButton(
                        modifier =
                            Modifier
                                .padding(16.dp),
                        onClick = {
                            if (page.tab == LockerTabs.Watchfaces) {
                                onNavChange(Routes.Home.STORE_WATCHFACES)
                            } else {
                                onNavChange(Routes.Home.STORE_APPS)
                            }
                        },
                        content = {
                            RebbleIcons.plusAdd()
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (page) {
                is HomePage.Locker -> {
                    Locker(page.tab, onTabChanged = {
                        onNavChange(it.navRoute)
                    })
                }
                is HomePage.Store -> {
                    Store(page.tab, onTabChanged = {
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
            }
        }
    }
}