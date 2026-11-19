package com.zc.bakamitai.feature.settings.presentation

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.settings.presentation.component.alertspreference.model.AlertsPreferenceUIState
import com.zc.bakamitai.feature.settings.presentation.component.segmentedpreference.model.SegmentUIState
import com.zc.bakamitai.feature.settings.presentation.component.themeoptions.model.ThemeOptionUIState
import com.zc.bakamitai.feature.settings.presentation.model.TimeFormatOption
import com.zc.bakamitai.feature.settings.presentation.model.WeekStartOption
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * The Settings screen (docs/6-settings.md §5.9).
 *
 * @property isLoading until the settings are first read; the preference cards show skeletons.
 * @property alerts the alerts switch; `null` while loading.
 * @property sourceCodeLabel the About row's URL, "github.com/zahichemaly/BakaMitai".
 * @property versionLabel the footer's "BakaMitai v1.0".
 */
@Immutable
data class SettingsState(
    val isLoading: Boolean = true,
    val weekStartSegments: ImmutableList<SegmentUIState<WeekStartOption>> = persistentListOf(),
    val timeFormatSegments: ImmutableList<SegmentUIState<TimeFormatOption>> = persistentListOf(),
    val themeOptions: ImmutableList<ThemeOptionUIState> = persistentListOf(),
    val alerts: AlertsPreferenceUIState? = null,
    val sourceCodeLabel: UiText? = null,
    val versionLabel: UiText? = null,
)
