package com.zc.bakamitai.feature.home.presentation.component.releaserow.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.home.presentation.model.EpisodeBadgeUIModel

/**
 * A row in Latest Releases.
 *
 * @property key unique in the list: the slug and the episode.
 * @property slug the show to open.
 * @property coverUrl absolute cover URL, or `null` for the placeholder.
 * @property timeLabel the release time in the device's zone.
 */
@Immutable
data class ReleaseRowUIModel(
    val key: String,
    val slug: String,
    val title: UiText,
    val coverUrl: String?,
    val timeLabel: UiText,
    val episodeBadge: EpisodeBadgeUIModel,
)
