package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkSort
import com.zc.bakamitai.feature.bookmarks.domain.repository.BookmarkSortRepository
import org.koin.core.annotation.Factory

/** Saves the order of the Bookmarks grid; it's the one shown after a relaunch. */
@Factory
class UpdateBookmarkSortUseCase(private val repository: BookmarkSortRepository) {
    suspend operator fun invoke(sort: BookmarkSort): Result<Unit> = repository.setSort(sort)
}
