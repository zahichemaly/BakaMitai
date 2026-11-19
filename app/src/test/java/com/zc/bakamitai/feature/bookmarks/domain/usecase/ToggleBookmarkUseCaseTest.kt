package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.common.util.FixedClock
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkToggle
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeBookmarkRepository
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseCheckScheduler
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseCheckScheduler.Call
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ToggleBookmarkUseCaseTest {

    private val now = Instant.parse("2026-11-19T08:00:00Z")
    private val repository = FakeBookmarkRepository()
    private val scheduler = FakeReleaseCheckScheduler()
    private val useCase = ToggleBookmarkUseCase(repository, scheduler, FixedClock(now))
    private val frieren = show("sousou-no-frieren", "Sousou no Frieren", "https://subsplease.org/a.jpg")
    private val dandadan = show("dandadan", "Dandadan")

    @Test
    fun `saves the show as of now, then removes it, asking for a check each time`() = runTest {
        assertEquals(Result.success(BookmarkToggle(isBookmarked = true, isFirstBookmark = true)), useCase(frieren))
        assertEquals(listOf(Bookmark(frieren, addedAt = now)), repository.bookmarks.value)

        assertEquals(Result.success(BookmarkToggle(isBookmarked = false, isFirstBookmark = false)), useCase(frieren))
        assertEquals(emptyList(), repository.bookmarks.value)
        assertEquals(listOf<Call>(Call.RequestCheck, Call.RequestCheck), scheduler.calls)
    }

    @Test
    fun `only the add that makes the first bookmark is the first`() = runTest {
        assertEquals(true, useCase(frieren).getOrThrow().isFirstBookmark)
        assertEquals(false, useCase(dandadan).getOrThrow().isFirstBookmark)
        assertEquals(false, useCase(frieren).getOrThrow().isFirstBookmark)
        assertEquals(false, useCase(dandadan).getOrThrow().isFirstBookmark)
        assertEquals(true, useCase(dandadan).getOrThrow().isFirstBookmark)
    }

    @Test
    fun `a failure is propagated and asks for no check`() = runTest {
        val error = DomainError.Unknown(IOException("disk full"))
        repository.toggleFailure = error

        assertEquals(error, useCase(frieren).exceptionOrNull())
        assertEquals(emptyList(), scheduler.calls)
    }
}
