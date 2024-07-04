package io.rebble.cobble.shared.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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
}

private val ENABLE_CALENDAR_KEY = booleanPreferencesKey("enable_calendar_sync")
private val SENSITIVE_DATA_LOGGING_KEY = booleanPreferencesKey("sensitive_data_logging")