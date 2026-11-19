package com.zc.bakamitai.feature.bookmarks.domain.repository

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkSort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException

/** The saved sort in memory; [failWrites] makes a write fail like a full disk. */
class FakeBookmarkSortRepository(initial: BookmarkSort = BookmarkSort.RECENTLY_ADDED) : BookmarkSortRepository {

    val sort = MutableStateFlow(initial)
    var failWrites = false

    override fun observeSort(): Flow<BookmarkSort> = sort

    override suspend fun setSort(sort: BookmarkSort): Result<Unit> {
        if (failWrites) return Result.failure(DomainError.Unknown(IOException("disk full")))
        this.sort.value = sort
        return Result.success(Unit)
    }
}
