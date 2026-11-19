package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.bookmark
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeBookmarkRepository
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseCheckScheduler
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseCheckScheduler.Call
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoveBookmarkUseCaseTest {

    private val repository = FakeBookmarkRepository()
    private val scheduler = FakeReleaseCheckScheduler()
    private val useCase = RemoveBookmarkUseCase(repository, scheduler)
    private val frieren = bookmark("frieren", "2026-11-10T00:00:00Z")

    @Test
    fun `returns the removed bookmark and asks for a check`() = runTest {
        repository.bookmarks.value = listOf(frieren)

        assertEquals(Result.success(frieren), useCase(ShowSlug("frieren")))
        assertEquals(emptyList(), repository.bookmarks.value)
        assertEquals(listOf<Call>(Call.RequestCheck), scheduler.calls)
    }

    @Test
    fun `removing a slug that isn't saved returns null and asks for nothing`() = runTest {
        assertEquals(Result.success(null), useCase(ShowSlug("frieren")))
        assertEquals(emptyList(), scheduler.calls)
    }

    @Test
    fun `a failure is propagated and asks for no check`() = runTest {
        repository.bookmarks.value = listOf(frieren)
        repository.removeFailure = DomainError.Unknown(IOException("disk"))

        assertEquals(repository.removeFailure, useCase(ShowSlug("frieren")).exceptionOrNull())
        assertEquals(emptyList(), scheduler.calls)
    }
}
