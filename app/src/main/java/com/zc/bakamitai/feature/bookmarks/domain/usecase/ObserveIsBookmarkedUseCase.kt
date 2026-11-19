package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.bookmarks.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

/** Whether a show is saved, following every change to the bookmarks. */
@Factory
class ObserveIsBookmarkedUseCase(private val repository: BookmarkRepository) {

    operator fun invoke(slug: ShowSlug): Flow<Boolean> = repository.observeBookmarks()
        .map { bookmarks -> bookmarks.any { it.show.slug == slug } }
        .distinctUntilChanged()
}
