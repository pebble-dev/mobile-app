package io.rebble.cobble.shared.ui.view

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.rebble.cobble.shared.ui.LocalTheme
import io.rebble.cobble.shared.ui.Theme
import io.rebble.cobble.shared.ui.nav.Routes
import io.rebble.cobble.shared.ui.view.dialogs.AppInstallDialog
import io.rebble.cobble.shared.ui.view.home.HomePage
import io.rebble.cobble.shared.ui.view.home.HomeScaffold
import io.rebble.cobble.shared.ui.view.home.locker.LockerTabs
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.decodeFromString
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
                        composable(Routes.Home.TEST_PAGE) {
                            HomeScaffold(HomePage.TestPage, onNavChange = navController::navigate)
                        }
                        dialog("${Routes.DIALOG_APP_INSTALL}?uri={uri}", arguments = listOf(navArgument("uri") {
                            nullable = false
                            type = NavType.StringType
                        })) {
                            val uri = it.arguments?.getString("uri") ?: return@dialog
                            AppInstallDialog(uri) {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}