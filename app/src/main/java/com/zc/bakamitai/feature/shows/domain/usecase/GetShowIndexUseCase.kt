package com.zc.bakamitai.feature.shows.domain.usecase

import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.feature.shows.domain.model.ShowIndex
import com.zc.bakamitai.feature.shows.domain.model.ShowIndexSection
import com.zc.bakamitai.feature.shows.domain.repository.ShowCatalogRepository
import org.koin.core.annotation.Factory

/**
 * The show catalogue as an A–Z index (docs/3-shows.md §4.3): exact (slug, title) duplicates removed, sorted by
 * title ignoring case, grouped by first letter. Titles that don't start with `A`–`Z` go under
 * [ShowIndexSection.OTHER], first.
 */
@Factory
class GetShowIndexUseCase(private val repository: ShowCatalogRepository) {

    /** @param forceRefresh bypass the catalogue cache (pull-to-refresh). */
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<ShowIndex> =
        repository.getAllShows(forceRefresh).map { shows ->
            val sections = shows
                .distinctBy { it.slug to it.title }
                .sortedWith(titleOrder)
                .groupBy { sectionLetter(it.title) }
                .toSortedMap()
                .map { (letter, sectionShows) -> ShowIndexSection(letter = letter, shows = sectionShows) }
            ShowIndex(sections)
        }

    /** `A`–`Z` from the title's first character, ignoring case; anything else is [ShowIndexSection.OTHER]. */
    private fun sectionLetter(title: String): Char =
        title.firstOrNull()?.uppercaseChar()?.takeIf { it in 'A'..'Z' } ?: ShowIndexSection.OTHER

    private companion object {
        /** Case-insensitive, then case-sensitive and by slug, so the order is stable. */
        val titleOrder: Comparator<Show> = compareBy<Show, String>(String.CASE_INSENSITIVE_ORDER) { it.title }
            .thenBy { it.title }
            .thenBy { it.slug.value }
    }
}
