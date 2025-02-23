package io.rebble.cobble.shared.ui.view.home.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.viewmodel.SettingsViewModel


@Composable
fun Settings(viewModel: SettingsViewModel = viewModel(), onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    Settings(settings = viewModel.settings, onNavigate = { /* TODO setup navigation in later PR */ }, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Settings(settings: List<SettingsViewModel.SettingsNavigationItem>, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        CenterAlignedTopAppBar(title = { Text("Settings") })
        Column(modifier.verticalScroll(rememberScrollState())) {
            SettingsAccountCard(modifier = Modifier.padding(16.dp))
            settings.forEach { item ->
                if (item.icon == SettingsViewModel.SettingsNavigationItem.SettingsIcons.DEVELOPER) {
                    HorizontalDivider(thickness = 2.dp)
                }
                SettingsNavigableListItem(
                        icon = {
                            when (item.icon) {
                                SettingsViewModel.SettingsNavigationItem.SettingsIcons.NOTIFICATIONS -> RebbleIcons.notification()
                                SettingsViewModel.SettingsNavigationItem.SettingsIcons.HEALTH -> RebbleIcons.healthHeart()
                                SettingsViewModel.SettingsNavigationItem.SettingsIcons.CALENDAR -> RebbleIcons.calendar()
                                SettingsViewModel.SettingsNavigationItem.SettingsIcons.MESSAGES -> RebbleIcons.smsMessages()
                                SettingsViewModel.SettingsNavigationItem.SettingsIcons.LANGUAGE -> RebbleIcons.systemLanguage()
                                SettingsViewModel.SettingsNavigationItem.SettingsIcons.ANALYTICS -> RebbleIcons.analytics()
                                SettingsViewModel.SettingsNavigationItem.SettingsIcons.ABOUT -> RebbleIcons.aboutApp()
                                SettingsViewModel.SettingsNavigationItem.SettingsIcons.DEVELOPER -> RebbleIcons.developerSettings()
                            }
                        },
                        title = item.title,
                        onClick = { onNavigate(item.navigation) }
                )
            }
            Spacer(modifier = Modifier.fillMaxWidth().height(16.dp))
        }
    }
}