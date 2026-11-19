package com.zc.bakamitai.feature.settings.presentation.component.segmentedpreference.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText

/**
 * One segment of a two-choice preference: "Monday (Default)".
 *
 * @property value the option a tap selects.
 * @property qualifier shown after the label, dimmed: "(Default)", "(AM/PM)".
 */
@Immutable
data class SegmentUIState<T>(val value: T, val label: UiText, val qualifier: UiText?, val isSelected: Boolean)
