package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.seen
import com.zc.bakamitai.feature.bookmarks.domain.model.EpisodeAlert
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseCheckOutcome
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatchUpdate
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeBookmarkRepository
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseAlertNotifier
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ApplyReleaseCheckUseCaseTest {

    private val repository = FakeBookmarkRepository()
    private val notifier = FakeReleaseAlertNotifier()
    private val useCase = ApplyReleaseCheckUseCase(repository, notifier)

    private val outcome = ReleaseCheckOutcome(
        updates = listOf(
            ReleaseWatchUpdate(
                ShowSlug("frieren"),
                seen("08", "2026-11-16T11:00:00Z"),
                Instant.parse("2026-11-16T12:00:00Z"),
            ),
        ),
        alerts = listOf(EpisodeAlert(show("frieren"), "08", Instant.parse("2026-11-16T11:00:00Z"), 1)),
        nextCheckAt = null,
    )

    @Test
    fun `saves the state, then posts the alerts`() = runTest {
        useCase(outcome)

        assertEquals(listOf(outcome.updates), repository.savedUpdates)
        assertEquals(listOf(outcome.alerts), notifier.posted)
    }

    @Test
    fun `a failed save posts nothing`() = runTest {
        repository.saveFailure = DomainError.Unknown(IOException("disk"))

        useCase(outcome)

        assertEquals(emptyList(), notifier.posted)
    }
}
