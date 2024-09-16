package io.rebble.cobble.shared.ui.view.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.view.home.locker.Locker

enum class HomePages {
    Locker
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScaffold(page: HomePages, modifier: Modifier = Modifier, topBarWindowInsets: WindowInsets = WindowInsets(0, 0, 0, 0)) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = { Text("Cobble") },
            )
        },
        bottomBar = {
            NavigationBar(
                    windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = page == HomePages.Locker,
                    onClick = { },
                    icon = { RebbleIcons.locker() },
                    label = { Text("Locker") }
                )
            }
        },
    ) {
        when (page) {
            HomePages.Locker -> {
                Locker()
            }
        }
    }
}