package com.zc.bakamitai.feature.shows.domain.usecase

import com.zc.bakamitai.feature.shows.domain.model.ShowIndex
import org.koin.core.annotation.Factory

/**
 * The shows of an index whose title contains the query, ignoring case (context.md §4.3: local search). The
 * query is trimmed; a blank one keeps the whole index. Sections left empty are dropped; the order is kept.
 */
@Factory
class FilterShowIndexUseCase {

    operator fun invoke(index: ShowIndex, query: String): ShowIndex {
        val needle = query.trim()
        if (needle.isEmpty()) return index
        val sections = index.sections.mapNotNull { section ->
            val matches = section.shows.filter { it.title.contains(needle, ignoreCase = true) }
            if (matches.isEmpty()) null else section.copy(shows = matches)
        }
        return ShowIndex(sections)
    }
}
