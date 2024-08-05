package io.rebble.cobble.shared.ui.view.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScaffold(modifier: Modifier = Modifier, topBarWindowInsets: WindowInsets = WindowInsets(0, 0, 0, 0)) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = { Text("Cobble") },
            )
        },
        bottomBar = {
            BottomNavigation(
                    windowInsets = WindowInsets.navigationBars
            ) {
                BottomNavigationItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
    ) {

    }
}