package com.zc.bakamitai.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import com.zc.bakamitai.common.domain.usecase.ObserveSettingsUseCase
import com.zc.bakamitai.common.ui.component.error.mapper.toErrorUIModel
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseAlertsStatus
import com.zc.bakamitai.feature.bookmarks.domain.usecase.GetReleaseAlertsStatusUseCase
import com.zc.bakamitai.feature.settings.domain.usecase.GetAppInfoUseCase
import com.zc.bakamitai.feature.settings.domain.usecase.UpdateNewEpisodeAlertsUseCase
import com.zc.bakamitai.feature.settings.domain.usecase.UpdateThemeModeUseCase
import com.zc.bakamitai.feature.settings.domain.usecase.UpdateTimeFormatUseCase
import com.zc.bakamitai.feature.settings.domain.usecase.UpdateWeekStartUseCase
import com.zc.bakamitai.feature.settings.presentation.component.alertspreference.mapper.toAlertsPreferenceUIState
import com.zc.bakamitai.feature.settings.presentation.mapper.toThemePicker
import com.zc.bakamitai.feature.settings.presentation.mapper.toTimeFormatPicker
import com.zc.bakamitai.feature.settings.presentation.mapper.toVersionLabel
import com.zc.bakamitai.feature.settings.presentation.mapper.toWeekStartPicker
import com.zc.bakamitai.feature.settings.presentation.model.ThemeOption
import com.zc.bakamitai.feature.settings.presentation.model.TimeFormatOption
import com.zc.bakamitai.feature.settings.presentation.model.WeekStartOption
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

/**
 * The user's preferences, from the settings store, with the alerts status and the app's version. A picker's dialog
 * is opened and closed here (it survives rotation); a choice closes it and is saved at once. The screen follows the
 * store, so a failed save leaves the stored value shown (and a snackbar). Reducer rules: docs/6-settings.md §5.9.
 */
@KoinViewModel
class SettingsViewModel(
    observeSettings: ObserveSettingsUseCase,
    getAppInfo: GetAppInfoUseCase,
    private val updateWeekStart: UpdateWeekStartUseCase,
    private val updateTimeFormat: UpdateTimeFormatUseCase,
    private val updateThemeMode: UpdateThemeModeUseCase,
    private val updateNewEpisodeAlerts: UpdateNewEpisodeAlertsUseCase,
    private val getReleaseAlertsStatus: GetReleaseAlertsStatusUseCase,
) : ViewModel() {

    /** The app's version (the footer) and source code URL (opened by the About row). Never exposed. */
    private val appInfo = getAppInfo()

    /** The last alerts status read; `null` until read. Never exposed. */
    private val alertsStatus = MutableStateFlow<ReleaseAlertsStatus?>(null)

    private val _state = MutableStateFlow(SettingsState(versionLabel = appInfo.toVersionLabel()))
    val state: StateFlow<SettingsState> =
        combine(_state, observeSettings(), alertsStatus) { state, settings, status ->
            state.copy(
                isLoading = false,
                weekStart = settings.weekStart.toWeekStartPicker(),
                timeFormat = settings.timeFormat.toTimeFormatPicker(),
                theme = settings.themeMode.toThemePicker(),
                alerts = settings.toAlertsPreferenceUIState(status),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = _state.value,
        )

    private val _effect = Channel<SettingsEffect>(Channel.BUFFERED)
    val effect: Flow<SettingsEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.ScreenStarted,
            is SettingsIntent.NotificationPermissionResult,
            -> viewModelScope.launch { refreshAlertsStatus() }

            SettingsIntent.BackClicked -> sendEffect(SettingsEffect.NavigateBack)

            is SettingsIntent.PickerClicked -> _state.update { it.copy(openPicker = intent.picker) }

            SettingsIntent.PickerDismissed -> closePicker()

            is SettingsIntent.WeekStartSelected -> saveChoice { updateWeekStart(intent.option.toWeekStart()) }

            is SettingsIntent.TimeFormatSelected -> saveChoice { updateTimeFormat(intent.option.toTimeFormat()) }

            is SettingsIntent.ThemeSelected -> saveChoice { updateThemeMode(intent.option.toThemeMode()) }

            is SettingsIntent.AlertsToggled -> toggleAlerts(intent.enabled)

            SettingsIntent.AllowNotificationsClicked -> sendEffect(SettingsEffect.RequestNotifications)

            SettingsIntent.AboutClicked -> sendEffect(SettingsEffect.OpenUrl(appInfo.sourceCodeUrl))
        }
    }

    /**
     * Closes the picker's dialog and saves its choice, even when it's the current value (an unchanged value isn't
     * emitted again). A failure is a snackbar: the store keeps, and the screen shows, the stored value.
     */
    private fun saveChoice(write: suspend () -> Result<Unit>) {
        closePicker()
        viewModelScope.launch {
            write().onFailure { error -> _effect.send(SettingsEffect.ShowError(error.toErrorUIModel())) }
        }
    }

    private fun closePicker() {
        _state.update { it.copy(openPicker = null) }
    }

    /** Saves the switch; switching on while Android blocks notifications asks for them right away. */
    private fun toggleAlerts(enabled: Boolean) {
        viewModelScope.launch {
            updateNewEpisodeAlerts(enabled)
                .onSuccess {
                    val status = refreshAlertsStatus()
                    if (enabled && status == ReleaseAlertsStatus.BLOCKED) {
                        _effect.send(SettingsEffect.RequestNotifications)
                    }
                }
                .onFailure { error -> _effect.send(SettingsEffect.ShowError(error.toErrorUIModel())) }
        }
    }

    private suspend fun refreshAlertsStatus(): ReleaseAlertsStatus =
        getReleaseAlertsStatus().also { alertsStatus.value = it }

    private fun sendEffect(effect: SettingsEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

private fun WeekStartOption.toWeekStart(): WeekStart = when (this) {
    WeekStartOption.MONDAY -> WeekStart.MONDAY
    WeekStartOption.SUNDAY -> WeekStart.SUNDAY
}

private fun TimeFormatOption.toTimeFormat(): TimeFormat = when (this) {
    TimeFormatOption.TWELVE_HOUR -> TimeFormat.TWELVE_HOUR
    TimeFormatOption.TWENTY_FOUR_HOUR -> TimeFormat.TWENTY_FOUR_HOUR
}

private fun ThemeOption.toThemeMode(): ThemeMode = when (this) {
    ThemeOption.SYSTEM -> ThemeMode.SYSTEM
    ThemeOption.LIGHT -> ThemeMode.LIGHT
    ThemeOption.DARK -> ThemeMode.DARK
}
