package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkSort
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeBookmarkSortRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveBookmarkSortUseCaseTest {

    @Test
    fun `emits the saved sort`() = runTest {
        val repository = FakeBookmarkSortRepository(BookmarkSort.TITLE)

        assertEquals(BookmarkSort.TITLE, ObserveBookmarkSortUseCase(repository)().first())
    }
}
