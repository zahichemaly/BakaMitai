package com.zc.bakamitai.common.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SettingsLocalDataSourceTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun TestScope.dataStore() =
        PreferenceDataStoreFactory.create(scope = backgroundScope) { File(tmp.root, "settings.preferences_pb") }

    @Test
    fun `an empty file reads as the defaults`() = runTest {
        assertEquals(AppSettings(), SettingsLocalDataSource(dataStore()).settings.first())
    }

    @Test
    fun `every setter round-trips`() = runTest {
        val dataSource = SettingsLocalDataSource(dataStore())

        dataSource.setWeekStart(WeekStart.SUNDAY)
        dataSource.setTimeFormat(TimeFormat.TWENTY_FOUR_HOUR)
        dataSource.setThemeMode(ThemeMode.DARK)
        dataSource.setNewEpisodeAlerts(false)

        assertEquals(
            AppSettings(
                weekStart = WeekStart.SUNDAY,
                timeFormat = TimeFormat.TWENTY_FOUR_HOUR,
                themeMode = ThemeMode.DARK,
                newEpisodeAlerts = false,
            ),
            dataSource.settings.first(),
        )
    }

    @Test
    fun `an unknown stored name reads as the default`() = runTest {
        val dataStore = dataStore()
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("week_start")] = "FRIDAY"
            preferences[stringPreferencesKey("theme_mode")] = "SEPIA"
            preferences[stringPreferencesKey("time_format")] = "TWENTY_FOUR_HOUR"
        }

        assertEquals(
            AppSettings(timeFormat = TimeFormat.TWENTY_FOUR_HOUR),
            SettingsLocalDataSource(dataStore).settings.first(),
        )
    }

    @Test
    fun `a read failure gives the defaults`() = runTest {
        assertEquals(AppSettings(), SettingsLocalDataSource(FailingPreferencesDataStore()).settings.first())
    }

    @Test
    fun `a write failure throws IOException`() = runTest {
        val dataSource = SettingsLocalDataSource(FailingPreferencesDataStore())

        assertFailsWith<IOException> { dataSource.setThemeMode(ThemeMode.LIGHT) }
    }

    @Test
    fun `writing the stored value emits nothing new`() = runTest {
        val dataSource = SettingsLocalDataSource(dataStore())

        dataSource.settings.test {
            assertEquals(AppSettings(), awaitItem())

            dataSource.setWeekStart(WeekStart.MONDAY)
            dataSource.setTimeFormat(TimeFormat.TWENTY_FOUR_HOUR)

            // The Monday write (the default) emitted nothing: the next item is the time format's.
            assertEquals(AppSettings(timeFormat = TimeFormat.TWENTY_FOUR_HOUR), awaitItem())
        }
    }
}
