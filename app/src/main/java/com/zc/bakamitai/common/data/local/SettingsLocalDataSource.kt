package com.zc.bakamitai.common.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

/**
 * The user's settings in the `settings` Preferences DataStore (docs/6-settings.md §4.3). Enums are stored by name;
 * a missing or unknown value reads as the default. Reads never fail; writes throw `IOException`.
 */
@Single
class SettingsLocalDataSource(private val dataStore: DataStore<Preferences>) {

    val settings: Flow<AppSettings> = dataStore.safeData()
        .map { preferences -> preferences.toAppSettings() }
        .distinctUntilChanged()

    suspend fun setWeekStart(value: WeekStart) {
        dataStore.edit { it[WEEK_START] = value.name }
    }

    suspend fun setTimeFormat(value: TimeFormat) {
        dataStore.edit { it[TIME_FORMAT] = value.name }
    }

    suspend fun setThemeMode(value: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = value.name }
    }

    suspend fun setNewEpisodeAlerts(enabled: Boolean) {
        dataStore.edit { it[NEW_EPISODE_ALERTS] = enabled }
    }

    private fun Preferences.toAppSettings(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            weekStart = enumOrNull<WeekStart>(WEEK_START) ?: defaults.weekStart,
            timeFormat = enumOrNull<TimeFormat>(TIME_FORMAT) ?: defaults.timeFormat,
            themeMode = enumOrNull<ThemeMode>(THEME_MODE) ?: defaults.themeMode,
            newEpisodeAlerts = this[NEW_EPISODE_ALERTS] ?: defaults.newEpisodeAlerts,
        )
    }

    private companion object {
        val WEEK_START = stringPreferencesKey("week_start")
        val TIME_FORMAT = stringPreferencesKey("time_format")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NEW_EPISODE_ALERTS = booleanPreferencesKey("new_episode_alerts")
    }
}
