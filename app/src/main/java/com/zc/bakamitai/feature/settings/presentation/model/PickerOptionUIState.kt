package com.zc.bakamitai.feature.settings.presentation.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText

/**
 * One option of a picker setting, a row of its dialog: "System default / Follows your device's dark & light mode".
 *
 * @property option the value a tap picks.
 * @property description a second line under the label, or none.
 */
@Immutable
data class PickerOptionUIState<out T>(
    val option: T,
    val label: UiText,
    val description: UiText?,
    val isSelected: Boolean,
)
