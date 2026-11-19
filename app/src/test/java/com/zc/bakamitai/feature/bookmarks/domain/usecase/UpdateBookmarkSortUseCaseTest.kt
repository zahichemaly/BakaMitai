package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkSort
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeBookmarkSortRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UpdateBookmarkSortUseCaseTest {

    private val repository = FakeBookmarkSortRepository()
    private val useCase = UpdateBookmarkSortUseCase(repository)

    @Test
    fun `saves the sort`() = runTest {
        assertEquals(Result.success(Unit), useCase(BookmarkSort.NEXT_AIRING))
        assertEquals(BookmarkSort.NEXT_AIRING, repository.sort.value)
    }

    @Test
    fun `a failed write is returned`() = runTest {
        repository.failWrites = true

        assertIs<DomainError.Unknown>(useCase(BookmarkSort.TITLE).exceptionOrNull())
        assertEquals(BookmarkSort.RECENTLY_ADDED, repository.sort.value)
    }
}
