package com.zc.bakamitai.feature.home.domain.usecase

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.feature.home.domain.HomeTestData.release
import com.zc.bakamitai.feature.home.domain.repository.FakeBroadcastRepository
import com.zc.bakamitai.feature.home.domain.repository.FakeBroadcastRepository.Call
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FindLatestEpisodeNumberUseCaseTest {

    private val repository = FakeBroadcastRepository()
    private val useCase = FindLatestEpisodeNumberUseCase(repository)
    private val timeZone = TimeZone.of("America/New_York")
    private val sasaki = show("sasaki-to-pii-chan-s2", "Sasaki to Pii-chan S2")

    @Test
    fun `picks the highest numeric episode of the same slug among known releases`() = runTest {
        val known = listOf(
            release("sasaki-to-pii-chan-s2", "06", "2026-11-11T12:03:07Z"),
            release("sasaki-to-pii-chan-s2", "07", "2026-11-18T12:03:04Z"),
            release("sasaki-to-pii-chan-s2", "01-24", "2026-11-18T13:00:00Z"),
            release("sasaki-to-pii-chan-s2", "Movie", "2026-11-18T14:00:00Z"),
            release("other-show", "99", "2026-11-18T12:00:00Z"),
        )

        assertEquals(7, useCase(sasaki, known, timeZone))
        assertTrue(repository.calls.isEmpty())
    }

    @Test
    fun `falls back to a title search filtered by slug`() = runTest {
        repository.searches["Sasaki to Pii-chan S2"] = Result.success(
            listOf(
                release("sasaki-to-pii-chan-s2", "06", "2026-11-11T12:03:07Z"),
                release("sasaki-to-pii-chan-s2", "05", "2026-11-04T12:04:05Z"),
                release("sasaki-to-pii-chan", "01-12", "2026-11-01T19:49:43Z"),
                release("sasaki-to-pii-chan", "12", "2024-03-22T12:31:26Z"),
            ),
        )
        val known = listOf(release("sasaki-to-pii-chan-s2", "01-24", "2026-11-18T13:00:00Z"))

        assertEquals(6, useCase(sasaki, known, timeZone))
        assertEquals(listOf<Call>(Call.Search("Sasaki to Pii-chan S2", timeZone)), repository.calls)
    }

    @Test
    fun `a failed search gives null`() = runTest {
        repository.searches["Sasaki to Pii-chan S2"] = Result.failure(DomainError.Network)

        assertNull(useCase(sasaki, emptyList(), timeZone))
    }

    @Test
    fun `nothing numeric gives null`() = runTest {
        repository.searches["Sasaki to Pii-chan S2"] = Result.success(
            listOf(
                release("sasaki-to-pii-chan-s2", "Movie", "2026-11-11T12:03:07Z"),
                release("sasaki-to-pii-chan-s2", "12.5", "2026-11-11T12:03:07Z"),
                release("sasaki-to-pii-chan", "12", "2024-03-22T12:31:26Z"),
            ),
        )

        assertNull(useCase(sasaki, emptyList(), timeZone))
    }
}
