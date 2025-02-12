package io.rebble.cobble.shared

import androidx.compose.ui.window.ComposeUIViewController
import androidx.navigation.compose.rememberNavController
import io.rebble.cobble.shared.ui.view.MainView

fun mainViewController() = ComposeUIViewController {
    val navHost = rememberNavController()
    MainView(navHost)
}