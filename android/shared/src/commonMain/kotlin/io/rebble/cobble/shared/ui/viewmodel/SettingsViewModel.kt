package io.rebble.cobble.shared.ui.viewmodel

import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    val settings: List<SettingsNavigationItem> = listOf(
            SettingsNavigationItem(
                    icon = SettingsNavigationItem.SettingsIcons.NOTIFICATIONS,
                    title = "Notification",
                    navigation = "notification_settings",
            ),
            SettingsNavigationItem(
                    icon = SettingsNavigationItem.SettingsIcons.HEALTH,
                    title = "Health",
                    navigation = "health_settings",
            ),
            SettingsNavigationItem(
                    icon = SettingsNavigationItem.SettingsIcons.CALENDAR,
                    title = "Calendar",
                    navigation = "calendar_settings",
            ),
            SettingsNavigationItem(
                    icon = SettingsNavigationItem.SettingsIcons.MESSAGES,
                    title = "Messages and canned replies",
                    navigation = "messages_settings",
            ),
            SettingsNavigationItem(
                    icon = SettingsNavigationItem.SettingsIcons.LANGUAGE,
                    title = "Language and dictation",
                    navigation = "language_settings",
            ),
            SettingsNavigationItem(
                    icon = SettingsNavigationItem.SettingsIcons.ANALYTICS,
                    title = "Analytics",
                    navigation = "analytics_settings",
            ),
            SettingsNavigationItem(
                    icon = SettingsNavigationItem.SettingsIcons.ABOUT,
                    title = "About and support",
                    navigation = "about_settings",
            ),
            SettingsNavigationItem(
                    icon = SettingsNavigationItem.SettingsIcons.DEVELOPER,
                    title = "Developer tools",
                    navigation = "developer_settings",
            ),
    )

    data class SettingsNavigationItem(
            val icon: SettingsIcons,
            val title: String,
            val navigation: String,
    ) {
        enum class SettingsIcons {
            NOTIFICATIONS,
            HEALTH,
            CALENDAR,
            MESSAGES,
            LANGUAGE,
            ANALYTICS,
            ABOUT,
            DEVELOPER,
        }
    }
}