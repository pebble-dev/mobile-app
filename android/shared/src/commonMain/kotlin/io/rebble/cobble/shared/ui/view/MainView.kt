package io.rebble.cobble.shared.ui.view

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.rebble.cobble.shared.ui.LocalTheme
import io.rebble.cobble.shared.ui.Theme
import io.rebble.cobble.shared.ui.nav.Routes
import io.rebble.cobble.shared.ui.view.home.HomePages
import io.rebble.cobble.shared.ui.view.home.HomeScaffold
import org.koin.compose.KoinContext

@Composable
fun MainView() {
    val navController = rememberNavController()
    KoinContext {
        DisableSelection {
            val theme = if (isSystemInDarkTheme()) Theme.dark else Theme.light

            CompositionLocalProvider(
                    LocalTheme provides theme,
            ) {
                MaterialTheme(
                        colorScheme = theme.materialColors
                ) {
                    NavHost(navController = navController, startDestination = Routes.Home.LOCKER) {
                        composable(Routes.Home.LOCKER) {
                            HomeScaffold(HomePages.Locker)
                        }
                    }
                }
            }
        }
    }
}