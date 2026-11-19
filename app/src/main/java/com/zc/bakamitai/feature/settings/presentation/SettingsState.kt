package com.zc.bakamitai.feature.settings.presentation

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.settings.presentation.component.alertspreference.model.AlertsPreferenceUIState
import com.zc.bakamitai.feature.settings.presentation.model.PickerUIState
import com.zc.bakamitai.feature.settings.presentation.model.SettingsPicker
import com.zc.bakamitai.feature.settings.presentation.model.ThemeOption
import com.zc.bakamitai.feature.settings.presentation.model.TimeFormatOption
import com.zc.bakamitai.feature.settings.presentation.model.WeekStartOption

/**
 * The Settings screen (docs/6-settings.md §5.9).
 *
 * @property isLoading until the settings are first read; the preference cards show skeletons.
 * @property weekStart the *First day of week* row and dialog; `null` while loading. [timeFormat] and [theme] alike.
 * @property alerts the alerts switch; `null` while loading.
 * @property openPicker the picker whose dialog is open, or none. Kept here, not in the composition, so the dialog
 *   survives rotation.
 * @property versionLabel the footer's "BakaMitai v2.0.0".
 */
@Immutable
data class SettingsState(
    val isLoading: Boolean = true,
    val weekStart: PickerUIState<WeekStartOption>? = null,
    val timeFormat: PickerUIState<TimeFormatOption>? = null,
    val theme: PickerUIState<ThemeOption>? = null,
    val alerts: AlertsPreferenceUIState? = null,
    val openPicker: SettingsPicker? = null,
    val versionLabel: UiText? = null,
)
