package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.common.util.FixedClock
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeBookmarkRepository
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ToggleBookmarkUseCaseTest {

    private val now = Instant.parse("2026-11-19T08:00:00Z")
    private val repository = FakeBookmarkRepository()
    private val useCase = ToggleBookmarkUseCase(repository, FixedClock(now))
    private val frieren = show("sousou-no-frieren", "Sousou no Frieren", "https://subsplease.org/a.jpg")

    @Test
    fun `saves the show as of now, then removes it`() = runTest {
        assertEquals(Result.success(true), useCase(frieren))
        assertEquals(listOf(Bookmark(frieren, addedAt = now)), repository.bookmarks.value)

        assertEquals(Result.success(false), useCase(frieren))
        assertEquals(emptyList(), repository.bookmarks.value)
    }

    @Test
    fun `propagates a failure`() = runTest {
        val error = DomainError.Unknown(IOException("disk full"))
        repository.toggleFailure = error

        assertEquals(error, useCase(frieren).exceptionOrNull())
    }
}
