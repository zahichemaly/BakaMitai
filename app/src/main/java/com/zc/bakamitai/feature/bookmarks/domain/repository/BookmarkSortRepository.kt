package com.zc.bakamitai.feature.bookmarks.domain.repository

import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkSort
import kotlinx.coroutines.flow.Flow

/** The saved order of the Bookmarks grid. Reads never fail; a failed write is a `DomainError`. */
interface BookmarkSortRepository {

    /** The saved sort, then every change. */
    fun observeSort(): Flow<BookmarkSort>

    suspend fun setSort(sort: BookmarkSort): Result<Unit>
}
