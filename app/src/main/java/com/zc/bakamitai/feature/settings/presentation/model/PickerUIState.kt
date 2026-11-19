package com.zc.bakamitai.feature.settings.presentation.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText
import kotlinx.collections.immutable.ImmutableList

/**
 * A setting picked from a list: its row shows [title] and [valueLabel]; a tap opens a dialog titled [title] that
 * lists [options].
 *
 * @property valueLabel the selected option's label, the row's supporting line: "Monday", "12-hour (AM/PM)".
 * @property options in order, exactly one selected.
 */
@Immutable
data class PickerUIState<out T>(
    val title: UiText,
    val valueLabel: UiText,
    val options: ImmutableList<PickerOptionUIState<T>>,
)
