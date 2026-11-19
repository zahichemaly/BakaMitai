package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.util.FixedClock
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.bookmark
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeBookmarkRepository
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseCheckScheduler
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseCheckScheduler.Call
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class RestoreBookmarkUseCaseTest {

    private val now = Instant.parse("2026-11-19T08:00:00Z")
    private val repository = FakeBookmarkRepository()
    private val scheduler = FakeReleaseCheckScheduler()
    private val useCase = RestoreBookmarkUseCase(repository, scheduler, FixedClock(now))
    private val frieren = bookmark("frieren", "2026-11-10T00:00:00Z")

    @Test
    fun `restores the bookmark with its original date, tracking from now, and asks for a check`() = runTest {
        assertEquals(Result.success(Unit), useCase(frieren))

        assertEquals(listOf(frieren to now), repository.restored)
        assertEquals(listOf(frieren), repository.bookmarks.value)
        assertEquals(listOf<Call>(Call.RequestCheck), scheduler.calls)
    }

    @Test
    fun `a failure is propagated and asks for no check`() = runTest {
        repository.restoreFailure = DomainError.Unknown(IOException("disk"))

        assertEquals(repository.restoreFailure, useCase(frieren).exceptionOrNull())
        assertEquals(emptyList(), scheduler.calls)
    }
}
