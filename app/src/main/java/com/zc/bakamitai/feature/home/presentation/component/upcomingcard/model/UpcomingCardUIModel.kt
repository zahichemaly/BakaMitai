package com.zc.bakamitai.feature.home.presentation.component.upcomingcard.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.home.presentation.model.EpisodeBadgeUIModel

/**
 * A card in the Upcoming carousel.
 *
 * @property key unique in the list: the slug and the scheduled time.
 * @property slug the show to open.
 * @property coverUrl absolute cover URL, or `null` for the placeholder.
 * @property episodeBadge the expected episode, or `null` when unknown.
 */
@Immutable
data class UpcomingCardUIModel(
    val key: String,
    val slug: String,
    val title: UiText,
    val coverUrl: String?,
    val timeLabel: UiText,
    val episodeBadge: EpisodeBadgeUIModel?,
)
