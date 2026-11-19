package com.zc.bakamitai.app

import app.cash.turbine.test
import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.repository.FakeSettingsRepository
import com.zc.bakamitai.common.domain.usecase.ObserveSettingsUseCase
import com.zc.bakamitai.common.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class AppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settings = FakeSettingsRepository()

    private fun createViewModel() = AppViewModel(ObserveSettingsUseCase(settings))

    @Test
    fun `starts not loaded, then loaded with the defaults`() = runTest {
        createViewModel().state.test {
            assertEquals(AppState(), awaitItem())
            assertEquals(AppState(isLoaded = true, darkThemeOverride = null, use24HourClock = false), awaitItem())
        }
    }

    @Test
    fun `the theme mode maps to a dark override, and the time format to the clock`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            skipItems(2)

            settings.settings.value = AppSettings(themeMode = ThemeMode.LIGHT)
            assertEquals(AppState(isLoaded = true, darkThemeOverride = false), awaitItem())

            settings.settings.value = AppSettings(themeMode = ThemeMode.DARK)
            assertEquals(AppState(isLoaded = true, darkThemeOverride = true), awaitItem())

            settings.settings.value = AppSettings(themeMode = ThemeMode.DARK, timeFormat = TimeFormat.TWENTY_FOUR_HOUR)
            assertEquals(AppState(isLoaded = true, darkThemeOverride = true, use24HourClock = true), awaitItem())

            settings.settings.value = AppSettings()
            assertEquals(AppState(isLoaded = true), awaitItem())
        }
    }
}
