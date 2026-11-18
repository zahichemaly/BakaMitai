package com.zc.bakamitai.feature.home.domain.usecase

import com.zc.bakamitai.feature.home.domain.model.Release
import com.zc.bakamitai.feature.home.domain.model.Show
import com.zc.bakamitai.feature.home.domain.model.ShowSlug
import com.zc.bakamitai.feature.home.domain.repository.BroadcastRepository
import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Factory

/**
 * The highest numeric episode already released for [Show] (context.md §4.1 step 4). Batches (`"01-24"`),
 * movies and anything else that isn't a whole number are ignored.
 */
@Factory
class FindLatestEpisodeNumberUseCase(private val repository: BroadcastRepository) {

    /**
     * Looks in [knownReleases] first, then searches the releases by title and keeps only the show's own slug
     * (a search also returns other seasons). `null` when nothing numeric was found or the search failed.
     */
    suspend operator fun invoke(show: Show, knownReleases: List<Release>, timeZone: TimeZone): Int? =
        knownReleases.latestEpisodeOf(show.slug)
            ?: repository.searchReleases(show.title, timeZone).getOrNull()?.latestEpisodeOf(show.slug)

    private fun List<Release>.latestEpisodeOf(slug: ShowSlug): Int? = asSequence()
        .filter { it.show.slug == slug }
        .mapNotNull { release -> release.episode.takeIf { it.all(Char::isDigit) }?.toIntOrNull() }
        .maxOrNull()
}
