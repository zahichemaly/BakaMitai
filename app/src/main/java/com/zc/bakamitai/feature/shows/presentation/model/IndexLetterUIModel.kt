package com.zc.bakamitai.feature.shows.presentation.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText

/**
 * A section's letter, shown by its header and its chip.
 *
 * @property letter `'A'`..`'Z'` or `'#'`; also the section's identity.
 * @property label what is shown: "A", "#".
 * @property description what TalkBack reads: "A", "Numbers and symbols".
 * @property isPrimaryAccent the header badge's accent, alternating by letter (A primary, B secondary, …; "#"
 * secondary), so it never changes while filtering.
 */
@Immutable
data class IndexLetterUIModel(
    val letter: Char,
    val label: UiText,
    val description: UiText,
    val isPrimaryAccent: Boolean,
)
