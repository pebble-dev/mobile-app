package io.rebble.cobble.shared.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import io.rebble.cobble.shared.ui.common.RebbleIcons

class SettingsViewModel : ViewModel() {

    val settings: List<SettingsNavigationItem> = listOf(
            SettingsNavigationItem(
                    icon = { RebbleIcons.notification() },
                    title = "Notification",
                    navigation = "notification_settings",
            ),
            SettingsNavigationItem(
                    icon = { RebbleIcons.healthHeart() },
                    title = "Health",
                    navigation = "health_settings",
            ),
            SettingsNavigationItem(
                    icon = { RebbleIcons.calendar() },
                    title = "Calendar",
                    navigation = "calendar_settings",
            ),
            SettingsNavigationItem(
                    icon = { RebbleIcons.smsMessages() },
                    title = "Messages and canned replies",
                    navigation = "messages_settings",
            ),
            SettingsNavigationItem(
                    icon = { RebbleIcons.systemLanguage() },
                    title = "Language and dictation",
                    navigation = "language_settings",
            ),
            SettingsNavigationItem(
                    icon = { RebbleIcons.analytics() },
                    title = "Analytics",
                    navigation = "analytics_settings",
            ),
            SettingsNavigationItem(
                    icon = { RebbleIcons.aboutApp() },
                    title = "About and support",
                    navigation = "about_settings",
            ),
            SettingsNavigationItem(
                    icon = { RebbleIcons.developerSettings() },
                    title = "Developer tools",
                    navigation = "developer_settings",
                    containsTopDivider = true
            ),
    )

    data class SettingsNavigationItem(
            val icon: @Composable () -> Unit,
            val title: String,
            val navigation: String,
            val containsTopDivider: Boolean = false,
    )
}