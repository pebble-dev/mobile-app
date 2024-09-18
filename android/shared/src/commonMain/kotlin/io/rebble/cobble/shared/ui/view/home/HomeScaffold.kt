package io.rebble.cobble.shared.ui.view.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.view.home.locker.Locker

enum class HomePages {
    Locker
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScaffold(page: HomePages) {
    Scaffold(
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
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (page) {
                HomePages.Locker -> {
                    Locker()
                }
            }
        }
    }
}