package com.zc.bakamitai.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.usecase.ObserveSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

/**
 * The app root's settings: the theme and the clock format, read from the settings store (docs/6-settings.md §5.1).
 * It's the app root, not a screen: one [state] and no intents or effects, since it has no user actions.
 */
@KoinViewModel
class AppViewModel(observeSettings: ObserveSettingsUseCase) : ViewModel() {

    val state: StateFlow<AppState> = observeSettings()
        .map { settings -> settings.toAppState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = AppState(),
        )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

private fun AppSettings.toAppState(): AppState = AppState(
    isLoaded = true,
    darkThemeOverride = when (themeMode) {
        ThemeMode.SYSTEM -> null
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    },
    use24HourClock = timeFormat == TimeFormat.TWENTY_FOUR_HOUR,
)
