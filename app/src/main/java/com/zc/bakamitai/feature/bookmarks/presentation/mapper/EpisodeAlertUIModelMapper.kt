package com.zc.bakamitai.feature.bookmarks.presentation.mapper

import com.zc.bakamitai.common.ui.mapper.toEpisodeTitle
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.bookmarks.domain.model.EpisodeAlert
import com.zc.bakamitai.feature.bookmarks.presentation.model.EpisodeAlertUIModel

/** "Episode 8 is out" under the show's title; "N new episodes" as the sub-text when a backlog had several. */
fun EpisodeAlert.toEpisodeAlertUIModel(): EpisodeAlertUIModel {
    val episodeTitle = episode.toEpisodeTitle()
    val title = UiText.StringValue(show.title)
    return EpisodeAlertUIModel(
        notificationId = show.slug.value.hashCode(),
        slug = show.slug.value,
        title = title,
        text = UiText.StringResource(StringResource.notification_episode_out, listOf(episodeTitle)),
        subText = if (newEpisodeCount > 1) {
            UiText.StringPlural(PluralResource.notification_more_new_episodes, newEpisodeCount, listOf(newEpisodeCount))
        } else {
            null
        },
        summaryLine = UiText.StringResource(StringResource.notification_summary_line, listOf(title, episodeTitle)),
    )
}
