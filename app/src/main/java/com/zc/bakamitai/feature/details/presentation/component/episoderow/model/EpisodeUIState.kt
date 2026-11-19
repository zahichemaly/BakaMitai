package com.zc.bakamitai.feature.details.presentation.component.episoderow.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.model.ResolutionUIModel
import kotlinx.collections.immutable.ImmutableList

/**
 * An episode or batch row, which expands to its downloads.
 *
 * @property key unique in the list: `"episode|<label>"` or `"batch|<label>"` (SubsPlease keys entries by label).
 * @property releasedLabel "Nov 17, 2026 · 12:38 PM", in the device's zone.
 * @property resolutions always three: 1080p, 720p, SD.
 */
@Immutable
data class EpisodeUIState(
    val key: String,
    val title: UiText,
    val releasedLabel: UiText,
    val resolutions: ImmutableList<ResolutionUIModel>,
    val isExpanded: Boolean = false,
)
