package com.zc.bakamitai.feature.details.presentation.mapper

import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.details.domain.model.Episode
import com.zc.bakamitai.feature.details.domain.model.ShowEpisodes
import com.zc.bakamitai.feature.details.presentation.component.episoderow.mapper.toEpisodeKey
import com.zc.bakamitai.feature.details.presentation.component.episoderow.mapper.toEpisodeUIState
import com.zc.bakamitai.feature.details.presentation.model.DownloadsUIState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.TimeZone

/**
 * The downloads section: [DownloadsUIState.Empty] without episodes or batches, otherwise the rows.
 *
 * @param expandedKeys the rows the user expanded (kept across a refresh or retry). Every row starts collapsed,
 * including the newest. Keys that no longer exist are ignored.
 */
fun ShowEpisodes.toDownloadsUIState(expandedKeys: Set<String> = emptySet()): DownloadsUIState {
    if (episodes.isEmpty() && batches.isEmpty()) return DownloadsUIState.Empty
    return DownloadsUIState.Content(
        countLabel = episodes.size.takeIf { it > 0 }?.let { count ->
            UiText.StringPlural(PluralResource.details_episode_count, count, listOf(count))
        },
        batches = batches.toRows(isBatch = true, timeZone = timeZone, expanded = expandedKeys),
        episodes = episodes.toRows(isBatch = false, timeZone = timeZone, expanded = expandedKeys),
        limitNote = if (isTruncated) {
            UiText.StringResource(StringResource.details_episode_limit_note, listOf(ShowEpisodes.EPISODE_LIMIT))
        } else {
            null
        },
    )
}

private fun List<Episode>.toRows(isBatch: Boolean, timeZone: TimeZone, expanded: Set<String>) =
    map { it.toEpisodeUIState(isBatch, timeZone, isExpanded = it.toEpisodeKey(isBatch) in expanded) }.toImmutableList()
