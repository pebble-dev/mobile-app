package io.rebble.cobble.shared.ui.view

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.rebble.cobble.shared.ui.LocalTheme
import io.rebble.cobble.shared.ui.Theme
import io.rebble.cobble.shared.ui.nav.Routes
import io.rebble.cobble.shared.ui.view.home.HomePage
import io.rebble.cobble.shared.ui.view.home.HomeScaffold
import io.rebble.cobble.shared.ui.view.home.locker.LockerTabs
import org.koin.compose.KoinContext

@Composable
fun MainView(navController: NavHostController = rememberNavController()) {
    KoinContext {
        DisableSelection {
            val theme = if (isSystemInDarkTheme()) Theme.dark else Theme.light

            CompositionLocalProvider(
                    LocalTheme provides theme,
            ) {
                MaterialTheme(
                        colorScheme = theme.materialColors
                ) {
                    NavHost(navController = navController, startDestination = Routes.Home.LOCKER_WATCHFACES) {
                        composable(Routes.Home.LOCKER_WATCHFACES) {
                            HomeScaffold(HomePage.Locker(LockerTabs.Watchfaces), onNavChange = navController::navigate)
                        }
                        composable(Routes.Home.LOCKER_APPS) {
                            HomeScaffold(HomePage.Locker(LockerTabs.Apps), onNavChange = navController::navigate)
                        }
                    }
                }
            }
        }
    }
}