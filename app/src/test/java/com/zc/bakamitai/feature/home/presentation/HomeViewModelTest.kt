package com.zc.bakamitai.feature.home.presentation

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.util.FixedTimeZoneProvider
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.util.FixedClock
import com.zc.bakamitai.common.util.MainDispatcherRule
import com.zc.bakamitai.feature.home.domain.HomeTestData.release
import com.zc.bakamitai.feature.home.domain.HomeTestData.scheduled
import com.zc.bakamitai.feature.home.domain.repository.FakeBroadcastRepository
import com.zc.bakamitai.feature.home.domain.repository.FakeBroadcastRepository.Call
import com.zc.bakamitai.feature.home.domain.usecase.FindLatestEpisodeNumberUseCase
import com.zc.bakamitai.feature.home.domain.usecase.GetTodayBroadcastsUseCase
import com.zc.bakamitai.feature.home.domain.usecase.GetTodayDateUseCase
import com.zc.bakamitai.feature.home.presentation.component.todaysummary.model.TodaySummaryUIModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBroadcastRepository()

    /** 15:30 in New York on Wed 18 Nov 2026. */
    private val clock = FixedClock(Instant.parse("2026-11-18T20:30:00Z"))
    private val timeZoneProvider = FixedTimeZoneProvider(TimeZone.of("America/New_York"))

    private fun createViewModel() = HomeViewModel(
        getTodayBroadcasts = GetTodayBroadcastsUseCase(
            repository = repository,
            findLatestEpisodeNumber = FindLatestEpisodeNumberUseCase(repository),
            clock = clock,
            timeZoneProvider = timeZoneProvider,
        ),
        getTodayDate = GetTodayDateUseCase(clock, timeZoneProvider),
    )

    private val networkError = ErrorUIModel(UiText.StringResource(StringResource.error_network), isRetryable = true)

    private fun scheduleCalls() = repository.calls.count { it is Call.Schedule }

    private fun givenTodayHasOneOfEach() {
        repository.schedule = Result.success(listOf(scheduled("later", LocalTime(18, 0))))
        repository.latestPages[1] = Result.success(listOf(release("kikansha", "19", "2026-11-18T16:17:09Z")))
    }

    @Test
    fun `the first load shows loading, then the mapped content`() = runTest {
        givenTodayHasOneOfEach()
        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(HomeState(isLoading = true), awaitItem())
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertFalse(loaded.isRefreshing)
            assertNull(loaded.error)
            assertEquals(
                TodaySummaryUIModel(
                    utcOffsetLabel = UiText.StringResource(StringResource.home_utc_offset, listOf("-5")),
                    upcomingCount = UiText.StringPlural(PluralResource.home_summary_airing, 1, listOf(1)),
                    releasedCount = UiText.StringPlural(PluralResource.home_summary_available, 1, listOf(1)),
                ),
                loaded.summary,
            )
            assertEquals(
                UiText.StringPlural(PluralResource.home_section_upcoming_count, 1, listOf(1)),
                loaded.upcomingCountLabel,
            )
            assertEquals(listOf("later"), loaded.upcoming.map { it.slug })
            assertEquals(listOf("kikansha"), loaded.latest.map { it.slug })
            assertEquals(UiText.StringValue("11:17 AM"), loaded.latest.single().timeLabel)
        }
    }

    @Test
    fun `a failed first load shows the error, and Retry loads the content`() = runTest {
        repository.schedule = Result.failure(DomainError.Network)
        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(HomeState(isLoading = true), awaitItem())
            assertEquals(
                HomeState(error = networkError),
                awaitItem(),
            )

            givenTodayHasOneOfEach()
            viewModel.onIntent(HomeIntent.Retry)

            assertEquals(HomeState(isLoading = true), awaitItem())
            val loaded = awaitItem()
            assertTrue(loaded.hasContent)
            assertNull(loaded.error)
            assertEquals(1, loaded.upcoming.size)
        }
    }

    @Test
    fun `a refresh with content shows the refresh indicator, then the updated content`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            skipItems(2)
            givenTodayHasOneOfEach()
            viewModel.onIntent(HomeIntent.Refresh)

            val refreshing = awaitItem()
            assertTrue(refreshing.isRefreshing)
            assertFalse(refreshing.isLoading)
            assertTrue(refreshing.isEmpty)
            val updated = awaitItem()
            assertFalse(updated.isRefreshing)
            assertEquals(listOf("kikansha"), updated.latest.map { it.slug })
        }
    }

    @Test
    fun `a failed refresh keeps the content and shows the error once`() = runTest {
        givenTodayHasOneOfEach()
        val viewModel = createViewModel()
        advanceUntilIdle()
        val content = viewModel.state.value

        turbineScope {
            val effects = viewModel.effect.testIn(backgroundScope)
            repository.schedule = Result.failure(DomainError.Timeout)
            viewModel.onIntent(HomeIntent.Refresh)
            advanceUntilIdle()

            assertEquals(content, viewModel.state.value)
            assertEquals(
                HomeEffect.ShowError(networkError),
                effects.awaitItem(),
            )
            effects.expectNoEvents()
        }
    }

    @Test
    fun `screen start reloads only on a new local day`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(1, scheduleCalls())

        viewModel.onIntent(HomeIntent.ScreenStarted)
        advanceUntilIdle()
        assertEquals(1, scheduleCalls())

        clock.instant += 1.days
        viewModel.onIntent(HomeIntent.ScreenStarted)
        advanceUntilIdle()
        assertEquals(2, scheduleCalls())

        viewModel.onIntent(HomeIntent.ScreenStarted)
        advanceUntilIdle()
        assertEquals(2, scheduleCalls())
    }

    @Test
    fun `screen start before a successful load does nothing`() = runTest {
        repository.schedule = Result.failure(DomainError.Network)
        val viewModel = createViewModel()
        advanceUntilIdle()

        clock.instant += 1.days
        viewModel.onIntent(HomeIntent.ScreenStarted)
        advanceUntilIdle()

        assertEquals(1, scheduleCalls())
    }

    @Test
    fun `a show click opens its details and the settings click opens settings`() = runTest {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onIntent(HomeIntent.ShowClicked("x"))
            assertEquals(HomeEffect.OpenDetails("x"), awaitItem())

            viewModel.onIntent(HomeIntent.SettingsClicked)
            assertEquals(HomeEffect.OpenSettings, awaitItem())
        }
    }

    @Test
    fun `a second refresh while loading is ignored`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate

        viewModel.onIntent(HomeIntent.Refresh)
        runCurrent()
        viewModel.onIntent(HomeIntent.Refresh)
        runCurrent()
        assertEquals(2, scheduleCalls())

        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(2, scheduleCalls())
        assertFalse(viewModel.state.value.isRefreshing)
    }
}
