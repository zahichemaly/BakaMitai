package com.zc.bakamitai.feature.details.presentation.component.episoderow.mapper

import com.zc.bakamitai.common.ui.mapper.toDateLabel
import com.zc.bakamitai.common.ui.mapper.toEpisodeTitle
import com.zc.bakamitai.common.ui.mapper.toTimeLabel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.details.domain.model.Episode
import com.zc.bakamitai.feature.details.presentation.component.episoderow.component.resolutionrow.mapper.toResolutionUIModels
import com.zc.bakamitai.feature.details.presentation.component.episoderow.model.EpisodeUIState
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val EPISODE_KEY_PREFIX = "episode|"
private const val BATCH_KEY_PREFIX = "batch|"

/** The row's list key; SubsPlease keys entries by label, so it's unique within its list. */
fun Episode.toEpisodeKey(isBatch: Boolean): String = (if (isBatch) BATCH_KEY_PREFIX else EPISODE_KEY_PREFIX) + label

/**
 * The row of an episode or batch. Its release line is in [timeZone]: the date, "Nov 17, 2026", and the shared
 * 12-hour time.
 */
fun Episode.toEpisodeUIState(isBatch: Boolean, timeZone: TimeZone, isExpanded: Boolean): EpisodeUIState =
    EpisodeUIState(
        key = toEpisodeKey(isBatch),
        title = label.toEpisodeTitle(),
        releasedLabel = UiText.StringResource(
            StringResource.details_episode_released,
            listOf(releasedAt.toLocalDateTime(timeZone).date.toDateLabel(), releasedAt.toTimeLabel(timeZone)),
        ),
        resolutions = downloads.toResolutionUIModels(),
        isExpanded = isExpanded,
    )
