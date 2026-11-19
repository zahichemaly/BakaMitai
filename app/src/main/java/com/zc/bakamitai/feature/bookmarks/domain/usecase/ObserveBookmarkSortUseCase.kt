package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkSort
import com.zc.bakamitai.feature.bookmarks.domain.repository.BookmarkSortRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

/** The saved order of the Bookmarks grid, then every change. */
@Factory
class ObserveBookmarkSortUseCase(private val repository: BookmarkSortRepository) {
    operator fun invoke(): Flow<BookmarkSort> = repository.observeSort()
}
