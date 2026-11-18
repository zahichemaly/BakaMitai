package com.zc.bakamitai.feature.home.presentation.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText

/**
 * An episode badge ("EP 08", "BATCH 01-24", "MOVIE").
 *
 * @property contentDescription what TalkBack reads instead of [label] ("Episode 08").
 * @property isSpecial batches, movies and other non-numeric episodes, drawn in the tertiary style.
 */
@Immutable
data class EpisodeBadgeUIModel(val label: UiText, val contentDescription: UiText, val isSpecial: Boolean)
