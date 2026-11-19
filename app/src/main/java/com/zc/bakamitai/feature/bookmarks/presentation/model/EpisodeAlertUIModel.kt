package com.zc.bakamitai.feature.bookmarks.presentation.model

import androidx.compose.runtime.Immutable
import com.zc.bakamitai.common.ui.text.UiText

/**
 * One new-episode notification.
 *
 * @property notificationId stable per show, so a newer episode replaces the show's previous notification.
 * @property slug the show its tap opens.
 * @property text "Episode 8 is out".
 * @property subText "2 new episodes" after a backlog, otherwise `null`.
 * @property summaryLine the show's line in the grouped summary, "Frieren · Episode 8".
 */
@Immutable
data class EpisodeAlertUIModel(
    val notificationId: Int,
    val slug: String,
    val title: UiText,
    val text: UiText,
    val subText: UiText?,
    val summaryLine: UiText,
)
