package com.zc.bakamitai.common.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.zc.bakamitai.common.data.local.FailingPreferencesDataStore
import com.zc.bakamitai.common.data.local.SettingsLocalDataSource
import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SettingsRepositoryImplTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun `writes succeed and reads pass through`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(tmp.root, "settings.preferences_pb")
        }
        val repository = SettingsRepositoryImpl(SettingsLocalDataSource(dataStore))

        assertEquals(AppSettings(), repository.observeSettings().first())
        assertEquals(Result.success(Unit), repository.setWeekStart(WeekStart.SUNDAY))
        assertEquals(Result.success(Unit), repository.setTimeFormat(TimeFormat.TWENTY_FOUR_HOUR))
        assertEquals(Result.success(Unit), repository.setThemeMode(ThemeMode.LIGHT))
        assertEquals(Result.success(Unit), repository.setNewEpisodeAlerts(false))
        assertEquals(
            AppSettings(WeekStart.SUNDAY, TimeFormat.TWENTY_FOUR_HOUR, ThemeMode.LIGHT, newEpisodeAlerts = false),
            repository.observeSettings().first(),
        )
    }

    @Test
    fun `a failing write is DomainError Unknown`() = runTest {
        val repository = SettingsRepositoryImpl(SettingsLocalDataSource(FailingPreferencesDataStore()))

        assertIs<DomainError.Unknown>(repository.setWeekStart(WeekStart.SUNDAY).exceptionOrNull())
        assertIs<DomainError.Unknown>(repository.setTimeFormat(TimeFormat.TWENTY_FOUR_HOUR).exceptionOrNull())
        assertIs<DomainError.Unknown>(repository.setThemeMode(ThemeMode.DARK).exceptionOrNull())
        assertIs<DomainError.Unknown>(repository.setNewEpisodeAlerts(false).exceptionOrNull())
        assertEquals(AppSettings(), repository.observeSettings().first())
    }
}
