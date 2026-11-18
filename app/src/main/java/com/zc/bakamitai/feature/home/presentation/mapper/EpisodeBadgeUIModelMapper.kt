package com.zc.bakamitai.feature.home.presentation.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.home.domain.model.Release
import com.zc.bakamitai.feature.home.domain.model.UpcomingBroadcast
import com.zc.bakamitai.feature.home.presentation.model.EpisodeBadgeUIModel

private const val MOVIE_EPISODE = "Movie"
private const val BATCH_SEPARATOR = '-'
private const val EPISODE_DIGITS = 2

/** "EP 07" for a numeric episode, "BATCH 01-24" for a range, "MOVIE", or the episode itself, uppercase. */
fun Release.toEpisodeBadgeUIModel(): EpisodeBadgeUIModel = when {
    episode.all(Char::isDigit) -> regularBadge(episode)

    BATCH_SEPARATOR in episode -> EpisodeBadgeUIModel(
        label = UiText.StringResource(StringResource.home_episode_batch, listOf(episode)),
        contentDescription = UiText.StringResource(StringResource.cd_episode_batch, listOf(episode)),
        isSpecial = true,
    )

    episode.equals(MOVIE_EPISODE, ignoreCase = true) -> EpisodeBadgeUIModel(
        label = UiText.StringResource(StringResource.home_episode_movie),
        contentDescription = UiText.StringResource(StringResource.cd_episode_movie),
        isSpecial = true,
    )

    else -> EpisodeBadgeUIModel(
        label = UiText.StringValue(episode.uppercase()),
        contentDescription = UiText.StringResource(StringResource.cd_episode, listOf(episode)),
        isSpecial = true,
    )
}

/** "EP 08" for the expected episode, or `null` (no badge) when it is unknown. */
fun UpcomingBroadcast.toEpisodeBadgeUIModel(): EpisodeBadgeUIModel? =
    episodeNumber?.let { number -> regularBadge(number.toString().padStart(EPISODE_DIGITS, '0')) }

private fun regularBadge(episode: String) = EpisodeBadgeUIModel(
    label = UiText.StringResource(StringResource.home_episode, listOf(episode)),
    contentDescription = UiText.StringResource(StringResource.cd_episode, listOf(episode)),
    isSpecial = false,
)
