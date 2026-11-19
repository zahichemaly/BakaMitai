package com.zc.bakamitai.feature.bookmarks.domain.usecase

import app.cash.turbine.test
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeBookmarkRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ObserveIsBookmarkedUseCaseTest {

    private val repository = FakeBookmarkRepository()
    private val useCase = ObserveIsBookmarkedUseCase(repository)

    private fun bookmark(slug: String) = Bookmark(show(slug), Instant.fromEpochMilliseconds(0))

    @Test
    fun `is true only for a saved slug`() = runTest {
        repository.bookmarks.value = listOf(bookmark("one-piece"))

        useCase(ShowSlug("one-piece")).test {
            assertEquals(true, awaitItem())
        }
        useCase(ShowSlug("sousou-no-frieren")).test {
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `follows changes to that slug only, without repeating a value`() = runTest {
        useCase(ShowSlug("one-piece")).test {
            assertEquals(false, awaitItem())

            repository.bookmarks.value = listOf(bookmark("sousou-no-frieren"))
            repository.bookmarks.value = listOf(bookmark("one-piece"), bookmark("sousou-no-frieren"))
            assertEquals(true, awaitItem())

            repository.bookmarks.value = listOf(bookmark("one-piece"))
            repository.bookmarks.value = emptyList()
            assertEquals(false, awaitItem())
            expectNoEvents()
        }
    }
}
