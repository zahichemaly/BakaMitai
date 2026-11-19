package com.zc.bakamitai.feature.settings.presentation

import app.cash.turbine.test
import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import com.zc.bakamitai.common.domain.repository.FakeSettingsRepository
import com.zc.bakamitai.common.domain.usecase.ObserveSettingsUseCase
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.util.MainDispatcherRule
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseAlertsStatus
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseAlertNotifier
import com.zc.bakamitai.feature.bookmarks.domain.usecase.GetReleaseAlertsStatusUseCase
import com.zc.bakamitai.feature.settings.domain.repository.FakeAppInfoRepository
import com.zc.bakamitai.feature.settings.domain.usecase.GetAppInfoUseCase
import com.zc.bakamitai.feature.settings.domain.usecase.UpdateNewEpisodeAlertsUseCase
import com.zc.bakamitai.feature.settings.domain.usecase.UpdateThemeModeUseCase
import com.zc.bakamitai.feature.settings.domain.usecase.UpdateTimeFormatUseCase
import com.zc.bakamitai.feature.settings.domain.usecase.UpdateWeekStartUseCase
import com.zc.bakamitai.feature.settings.presentation.component.alertspreference.mapper.toAlertsPreferenceUIState
import com.zc.bakamitai.feature.settings.presentation.mapper.toThemePicker
import com.zc.bakamitai.feature.settings.presentation.mapper.toTimeFormatPicker
import com.zc.bakamitai.feature.settings.presentation.mapper.toWeekStartPicker
import com.zc.bakamitai.feature.settings.presentation.model.SettingsPicker
import com.zc.bakamitai.feature.settings.presentation.model.ThemeOption
import com.zc.bakamitai.feature.settings.presentation.model.TimeFormatOption
import com.zc.bakamitai.feature.settings.presentation.model.WeekStartOption
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settings = FakeSettingsRepository()
    private val notifier = FakeReleaseAlertNotifier()

    private val versionLabel = UiText.StringResource(StringResource.settings_footer_version, listOf("1.0"))
    private val genericError = ErrorUIModel(UiText.StringResource(StringResource.error_generic), isRetryable = true)

    private fun createViewModel() = SettingsViewModel(
        observeSettings = ObserveSettingsUseCase(settings),
        getAppInfo = GetAppInfoUseCase(FakeAppInfoRepository()),
        updateWeekStart = UpdateWeekStartUseCase(settings),
        updateTimeFormat = UpdateTimeFormatUseCase(settings),
        updateThemeMode = UpdateThemeModeUseCase(settings),
        updateNewEpisodeAlerts = UpdateNewEpisodeAlertsUseCase(settings),
        getReleaseAlertsStatus = GetReleaseAlertsStatusUseCase(settings, notifier),
    )

    /** Keeps the `WhileSubscribed` state collected, so `state.value` follows every update. */
    private fun TestScope.subscribedViewModel(): SettingsViewModel = createViewModel().also { viewModel ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        runCurrent()
    }

    private fun expectedContent(appSettings: AppSettings, status: ReleaseAlertsStatus?) = SettingsState(
        isLoading = false,
        weekStart = appSettings.weekStart.toWeekStartPicker(),
        timeFormat = appSettings.timeFormat.toTimeFormatPicker(),
        theme = appSettings.themeMode.toThemePicker(),
        alerts = appSettings.toAlertsPreferenceUIState(status),
        versionLabel = versionLabel,
    )

    @Test
    fun `starts loading with the app info, then shows the stored settings`() = runTest {
        settings.settings.value = AppSettings(weekStart = WeekStart.SUNDAY, themeMode = ThemeMode.LIGHT)

        createViewModel().state.test {
            assertEquals(SettingsState(versionLabel = versionLabel), awaitItem())
            assertEquals(expectedContent(settings.settings.value, status = null), awaitItem())
        }
    }

    @Test
    fun `each selection saves the matching value`() = runTest {
        val viewModel = subscribedViewModel()

        viewModel.onIntent(SettingsIntent.WeekStartSelected(WeekStartOption.SUNDAY))
        viewModel.onIntent(SettingsIntent.TimeFormatSelected(TimeFormatOption.TWENTY_FOUR_HOUR))
        viewModel.onIntent(SettingsIntent.ThemeSelected(ThemeOption.DARK))
        runCurrent()

        assertEquals(listOf<Any>(WeekStart.SUNDAY, TimeFormat.TWENTY_FOUR_HOUR, ThemeMode.DARK), settings.writes)
        val expected = AppSettings(WeekStart.SUNDAY, TimeFormat.TWENTY_FOUR_HOUR, ThemeMode.DARK)
        assertEquals(expected, settings.settings.value)
        assertEquals(expectedContent(expected, status = ReleaseAlertsStatus.ON), viewModel.state.value)
    }

    @Test
    fun `the other options save their values too`() = runTest {
        settings.settings.value = AppSettings(WeekStart.SUNDAY, TimeFormat.TWENTY_FOUR_HOUR, ThemeMode.DARK)
        val viewModel = subscribedViewModel()

        viewModel.onIntent(SettingsIntent.WeekStartSelected(WeekStartOption.MONDAY))
        viewModel.onIntent(SettingsIntent.TimeFormatSelected(TimeFormatOption.TWELVE_HOUR))
        viewModel.onIntent(SettingsIntent.ThemeSelected(ThemeOption.LIGHT))
        viewModel.onIntent(SettingsIntent.ThemeSelected(ThemeOption.SYSTEM))
        runCurrent()

        assertEquals(
            listOf<Any>(WeekStart.MONDAY, TimeFormat.TWELVE_HOUR, ThemeMode.LIGHT, ThemeMode.SYSTEM),
            settings.writes,
        )
    }

    @Test
    fun `a failed save closes the dialog, shows the error and keeps the stored value`() = runTest {
        settings.failWrites = true
        val viewModel = subscribedViewModel()
        viewModel.onIntent(SettingsIntent.PickerClicked(SettingsPicker.THEME))

        viewModel.effect.test {
            viewModel.onIntent(SettingsIntent.ThemeSelected(ThemeOption.DARK))
            assertEquals(SettingsEffect.ShowError(genericError), awaitItem())
        }
        assertNull(viewModel.state.value.openPicker)
        assertEquals(ThemeMode.SYSTEM.toThemePicker(), viewModel.state.value.theme)
    }

    @Test
    fun `tapping a picker row opens its dialog, and dismissing it closes it without a save`() = runTest {
        val viewModel = subscribedViewModel()

        SettingsPicker.entries.forEach { picker ->
            viewModel.onIntent(SettingsIntent.PickerClicked(picker))
            runCurrent()
            assertEquals(picker, viewModel.state.value.openPicker)

            viewModel.onIntent(SettingsIntent.PickerDismissed)
            runCurrent()
            assertNull(viewModel.state.value.openPicker)
        }
        assertEquals(emptyList<Any>(), settings.writes)
        assertEquals(expectedContent(AppSettings(), status = null), viewModel.state.value)
    }

    @Test
    fun `picking an option in each dialog saves it and closes the dialog`() = runTest {
        val viewModel = subscribedViewModel()

        viewModel.onIntent(SettingsIntent.PickerClicked(SettingsPicker.WEEK_START))
        viewModel.onIntent(SettingsIntent.WeekStartSelected(WeekStartOption.SUNDAY))
        runCurrent()
        assertNull(viewModel.state.value.openPicker)
        assertEquals(WeekStart.SUNDAY.toWeekStartPicker(), viewModel.state.value.weekStart)

        viewModel.onIntent(SettingsIntent.PickerClicked(SettingsPicker.TIME_FORMAT))
        viewModel.onIntent(SettingsIntent.TimeFormatSelected(TimeFormatOption.TWENTY_FOUR_HOUR))
        runCurrent()
        assertNull(viewModel.state.value.openPicker)
        assertEquals(TimeFormat.TWENTY_FOUR_HOUR.toTimeFormatPicker(), viewModel.state.value.timeFormat)

        viewModel.onIntent(SettingsIntent.PickerClicked(SettingsPicker.THEME))
        viewModel.onIntent(SettingsIntent.ThemeSelected(ThemeOption.LIGHT))
        runCurrent()
        assertNull(viewModel.state.value.openPicker)
        assertEquals(ThemeMode.LIGHT.toThemePicker(), viewModel.state.value.theme)

        assertEquals(listOf<Any>(WeekStart.SUNDAY, TimeFormat.TWENTY_FOUR_HOUR, ThemeMode.LIGHT), settings.writes)
    }

    @Test
    fun `picking the current value closes the dialog and saves it again, unchanged`() = runTest {
        val viewModel = subscribedViewModel()
        viewModel.onIntent(SettingsIntent.PickerClicked(SettingsPicker.WEEK_START))
        runCurrent()

        viewModel.effect.test {
            viewModel.onIntent(SettingsIntent.WeekStartSelected(WeekStartOption.MONDAY))
            runCurrent()
            expectNoEvents()
        }

        assertNull(viewModel.state.value.openPicker)
        assertEquals(listOf<Any>(WeekStart.MONDAY), settings.writes)
        assertEquals(AppSettings(), settings.settings.value)
        assertEquals(expectedContent(AppSettings(), status = null), viewModel.state.value)
    }

    @Test
    fun `an open dialog stays open when the settings change elsewhere`() = runTest {
        val viewModel = subscribedViewModel()
        viewModel.onIntent(SettingsIntent.PickerClicked(SettingsPicker.THEME))
        runCurrent()

        settings.settings.value = AppSettings(themeMode = ThemeMode.DARK)
        runCurrent()

        assertEquals(SettingsPicker.THEME, viewModel.state.value.openPicker)
        assertEquals(ThemeMode.DARK.toThemePicker(), viewModel.state.value.theme)
    }

    @Test
    fun `screen start reads the alerts status`() = runTest {
        notifier.enabled = false
        val viewModel = subscribedViewModel()
        assertFalse(viewModel.state.value.alerts?.isBlocked ?: true)

        viewModel.onIntent(SettingsIntent.ScreenStarted)
        runCurrent()

        assertEquals(AppSettings().toAlertsPreferenceUIState(ReleaseAlertsStatus.BLOCKED), viewModel.state.value.alerts)
        assertTrue(viewModel.state.value.alerts?.isBlocked == true)
    }

    @Test
    fun `switching alerts on while Android blocks them asks for notifications`() = runTest {
        settings.settings.value = AppSettings(newEpisodeAlerts = false)
        notifier.enabled = false
        val viewModel = subscribedViewModel()

        viewModel.effect.test {
            viewModel.onIntent(SettingsIntent.AlertsToggled(enabled = true))
            assertEquals(SettingsEffect.RequestNotifications, awaitItem())
        }
        assertEquals(listOf<Any>(true), settings.writes)
        assertTrue(viewModel.state.value.alerts?.isBlocked == true)
    }

    @Test
    fun `switching alerts on while Android allows them, or off, asks for nothing`() = runTest {
        settings.settings.value = AppSettings(newEpisodeAlerts = false)
        val viewModel = subscribedViewModel()

        viewModel.effect.test {
            viewModel.onIntent(SettingsIntent.AlertsToggled(enabled = true))
            runCurrent()
            notifier.enabled = false
            viewModel.onIntent(SettingsIntent.AlertsToggled(enabled = false))
            runCurrent()

            expectNoEvents()
        }
        assertEquals(listOf<Any>(true, false), settings.writes)
        assertEquals(
            AppSettings(newEpisodeAlerts = false).toAlertsPreferenceUIState(null),
            viewModel.state.value.alerts,
        )
    }

    @Test
    fun `a failed alerts save shows the error`() = runTest {
        settings.failWrites = true
        val viewModel = subscribedViewModel()

        viewModel.effect.test {
            viewModel.onIntent(SettingsIntent.AlertsToggled(enabled = false))
            assertEquals(SettingsEffect.ShowError(genericError), awaitItem())
        }
        assertTrue(viewModel.state.value.alerts?.isChecked == true)
    }

    @Test
    fun `Allow notifications asks for them, and the permission's answer re-reads the status`() = runTest {
        notifier.enabled = false
        val viewModel = subscribedViewModel()
        viewModel.onIntent(SettingsIntent.ScreenStarted)
        runCurrent()

        viewModel.effect.test {
            viewModel.onIntent(SettingsIntent.AllowNotificationsClicked)
            assertEquals(SettingsEffect.RequestNotifications, awaitItem())
        }

        notifier.enabled = true
        viewModel.onIntent(SettingsIntent.NotificationPermissionResult(granted = true))
        runCurrent()

        assertFalse(viewModel.state.value.alerts?.isBlocked ?: true)
    }

    @Test
    fun `About opens the source code, and Back navigates back`() = runTest {
        val viewModel = subscribedViewModel()

        viewModel.effect.test {
            viewModel.onIntent(SettingsIntent.AboutClicked)
            assertEquals(SettingsEffect.OpenUrl("https://github.com/zahichemaly/BakaMitai"), awaitItem())
            viewModel.onIntent(SettingsIntent.BackClicked)
            assertEquals(SettingsEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `a change from elsewhere updates the screen`() = runTest {
        val viewModel = subscribedViewModel()

        settings.settings.value = AppSettings(timeFormat = TimeFormat.TWENTY_FOUR_HOUR)
        runCurrent()

        assertEquals(TimeFormat.TWENTY_FOUR_HOUR.toTimeFormatPicker(), viewModel.state.value.timeFormat)
    }
}
