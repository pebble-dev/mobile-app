package io.rebble.cobble.shared.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

const val DEFAULT_MUTED_PACKAGES_VERSION = 1
val defaultMutedPackages = setOf(
        "com.google.android.googlequicksearchbox",
        "de.itgecko.sharedownloader",
        "com.android.vending",
        "com.android.settings",
        "com.google.android.gms",
        "com.google.android.music",
        "com.android.chrome",
        "com.htc.vowifi",
        "com.android.providers.downloads",
        "org.mozilla.firefox",
        "com.htc.album",
        "com.dropbox.android",
        "com.lookout",
        "com.lastpass.lpandroid"
)

class KMPPrefs: KoinComponent {
    private val dataStore: DataStore<Preferences> by inject()

    val calendarSyncEnabled = dataStore.data.map { preferences ->
        preferences[ENABLE_CALENDAR_KEY] ?: true
    }
    suspend fun setCalendarSyncEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ENABLE_CALENDAR_KEY] = enabled
        }
    }

    val sensitiveDataLoggingEnabled = dataStore.data.map { preferences ->
        preferences[SENSITIVE_DATA_LOGGING_KEY] ?: false
    }
    suspend fun setSensitiveDataLoggingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SENSITIVE_DATA_LOGGING_KEY] = enabled
        }
    }

    val defaultMutedPackagesVersion = dataStore.data.map { preferences ->
        preferences[DEFAULT_MUTED_PACKAGES_VERSION_KEY] ?: 0
    }
    suspend fun setDefaultMutedPackagesVersion(version: Int) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_MUTED_PACKAGES_VERSION_KEY] = version
        }
    }

    val mutedPackages = dataStore.data.map { preferences ->
        preferences[MUTED_PACKAGES_KEY] ?: setOf()
    }
    suspend fun setMutedPackages(packages: Set<String>) {
        dataStore.edit { preferences ->
            preferences[MUTED_PACKAGES_KEY] = packages
        }
    }
}

private val ENABLE_CALENDAR_KEY = booleanPreferencesKey("enable_calendar_sync")
private val SENSITIVE_DATA_LOGGING_KEY = booleanPreferencesKey("sensitive_data_logging")
private val DEFAULT_MUTED_PACKAGES_VERSION_KEY = intPreferencesKey("default_muted_packages_version")
private val MUTED_PACKAGES_KEY = stringSetPreferencesKey("muted_packages")