package com.zc.bakamitai.feature.schedule.presentation

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowTestData.scheduled
import com.zc.bakamitai.common.domain.model.WeekStart
import com.zc.bakamitai.common.domain.repository.FakeSettingsRepository
import com.zc.bakamitai.common.domain.usecase.GetSettingsUseCase
import com.zc.bakamitai.common.domain.usecase.GetTodayDateUseCase
import com.zc.bakamitai.common.domain.util.FixedTimeZoneProvider
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.util.FixedClock
import com.zc.bakamitai.common.util.MainDispatcherRule
import com.zc.bakamitai.feature.schedule.domain.repository.FakeScheduleRepository
import com.zc.bakamitai.feature.schedule.domain.repository.FakeScheduleRepository.Call
import com.zc.bakamitai.feature.schedule.domain.usecase.GetWeeklyScheduleUseCase
import com.zc.bakamitai.feature.schedule.presentation.model.StatusFilter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class ScheduleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeScheduleRepository()
    private val settings = FakeSettingsRepository()
    private val newYork = TimeZone.of("America/New_York")

    /** 12:00 in New York on Thu 19 Nov 2026. */
    private val clock = FixedClock(Instant.parse("2026-11-19T17:00:00Z"))
    private val timeZoneProvider = FixedTimeZoneProvider(newYork)

    private val networkError = ErrorUIModel(UiText.StringResource(StringResource.error_network), isRetryable = true)

    private fun createViewModel() = ScheduleViewModel(
        getWeeklySchedule = GetWeeklyScheduleUseCase(repository, clock, timeZoneProvider),
        getTodayDate = GetTodayDateUseCase(clock, timeZoneProvider),
        getSettings = GetSettingsUseCase(settings),
    )

    private fun weeklyCalls() = repository.calls.filterIsInstance<Call.Weekly>()

    private fun ScheduleState.slugs() = day?.cards?.map { it.slug }

    /** Thursday (today): two aired shows and one upcoming; Friday: two shows without status. */
    private fun givenAWeek(upcoming: Boolean = true) {
        repository.weekly = Result.success(
            mapOf(
                DayOfWeek.THURSDAY to listOf(
                    scheduled("a", LocalTime(8, 0), aired = null),
                    scheduled("b", LocalTime(10, 0), aired = null),
                    scheduled("c", LocalTime(20, 0), aired = null),
                ),
                DayOfWeek.FRIDAY to listOf(
                    scheduled("d", LocalTime(9, 0), aired = null),
                    scheduled("e", LocalTime(11, 0), aired = null),
                ),
            ),
        )
        repository.today = Result.success(
            listOf(
                scheduled("a", LocalTime(8, 0), aired = true),
                scheduled("b", LocalTime(10, 0), aired = true),
                scheduled("c", LocalTime(20, 0), aired = !upcoming),
            ),
        )
    }

    @Test
    fun `the first load shows loading, then today's content`() = runTest {
        givenAWeek()
        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(ScheduleState(isLoading = true), awaitItem())
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertFalse(loaded.isRefreshing)
            assertNull(loaded.error)
            assertEquals(7, loaded.days.size)
            assertEquals(DayOfWeek.MONDAY, loaded.days.first().dayOfWeek)
            assertEquals(listOf(DayOfWeek.THURSDAY), loaded.days.filter { it.isToday }.map { it.dayOfWeek })
            assertEquals(DayOfWeek.THURSDAY, loaded.selectedDay)
            assertEquals(StatusFilter.ALL, loaded.statusFilter)
            assertEquals(
                UiText.StringResource(StringResource.day_thursday),
                loaded.day?.header?.title,
            )
            assertEquals(listOf("a", "b", "c"), loaded.slugs())
            assertEquals(3, loaded.day?.header?.filters?.size)
        }
        assertEquals(listOf(Call.Weekly(newYork, forceRefresh = false)), weeklyCalls())
    }

    @Test
    fun `a failed first load shows the error, and Retry loads the content`() = runTest {
        repository.weekly = Result.failure(DomainError.Network)
        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(ScheduleState(isLoading = true), awaitItem())
            assertEquals(ScheduleState(error = networkError), awaitItem())

            givenAWeek()
            viewModel.onIntent(ScheduleIntent.Retry)

            assertEquals(ScheduleState(isLoading = true), awaitItem())
            val loaded = awaitItem()
            assertTrue(loaded.hasContent)
            assertNull(loaded.error)
            assertEquals(DayOfWeek.THURSDAY, loaded.selectedDay)
        }
    }

    @Test
    fun `selecting a day shows its cards, and the filter applies only to today`() = runTest {
        givenAWeek()
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(ScheduleIntent.DaySelected(DayOfWeek.FRIDAY))
        val friday = viewModel.state.value
        assertEquals(DayOfWeek.FRIDAY, friday.selectedDay)
        assertEquals(listOf("d", "e"), friday.slugs())
        assertTrue(friday.day?.header?.filters.orEmpty().isEmpty())
        assertTrue(friday.day?.cards.orEmpty().all { it.status == null })

        viewModel.onIntent(ScheduleIntent.FilterSelected(StatusFilter.UPCOMING))
        assertEquals(StatusFilter.UPCOMING, viewModel.state.value.statusFilter)
        assertEquals(listOf("d", "e"), viewModel.state.value.slugs())

        viewModel.onIntent(ScheduleIntent.DaySelected(DayOfWeek.THURSDAY))
        assertEquals(listOf("c"), viewModel.state.value.slugs())
        assertEquals(true, viewModel.state.value.day?.cards?.single()?.status?.isUpcoming)
    }

    @Test
    fun `a filter that matches nothing shows the reset action, and reset shows every card`() = runTest {
        givenAWeek(upcoming = false)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(ScheduleIntent.FilterSelected(StatusFilter.UPCOMING))
        val filtered = viewModel.state.value
        assertTrue(filtered.day?.cards.orEmpty().isEmpty())
        assertEquals(true, filtered.day?.empty?.showResetAction)

        viewModel.onIntent(ScheduleIntent.ResetFilterClicked)
        val reset = viewModel.state.value
        assertEquals(StatusFilter.ALL, reset.statusFilter)
        assertEquals(listOf("a", "b", "c"), reset.slugs())
        assertNull(reset.day?.empty)
    }

    @Test
    fun `a day selection before any content is ignored`() = runTest {
        repository.weekly = Result.failure(DomainError.Network)
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(ScheduleIntent.DaySelected(DayOfWeek.FRIDAY))

        assertEquals(ScheduleState(error = networkError), viewModel.state.value)
    }

    @Test
    fun `a refresh shows the indicator, bypasses the cache and keeps the selection`() = runTest {
        givenAWeek()
        val viewModel = createViewModel()

        viewModel.state.test {
            skipItems(2)
            viewModel.onIntent(ScheduleIntent.DaySelected(DayOfWeek.FRIDAY))
            assertEquals(DayOfWeek.FRIDAY, awaitItem().selectedDay)

            viewModel.onIntent(ScheduleIntent.Refresh)
            val refreshing = awaitItem()
            assertTrue(refreshing.isRefreshing)
            assertFalse(refreshing.isLoading)
            assertTrue(refreshing.hasContent)
            val refreshed = awaitItem()
            assertFalse(refreshed.isRefreshing)
            assertEquals(DayOfWeek.FRIDAY, refreshed.selectedDay)
            assertEquals(listOf("d", "e"), refreshed.slugs())
        }
        assertEquals(
            listOf(Call.Weekly(newYork, forceRefresh = false), Call.Weekly(newYork, forceRefresh = true)),
            weeklyCalls(),
        )
    }

    @Test
    fun `a failed refresh keeps the content and shows the error once`() = runTest {
        givenAWeek()
        val viewModel = createViewModel()
        advanceUntilIdle()
        val content = viewModel.state.value

        turbineScope {
            val effects = viewModel.effect.testIn(backgroundScope)
            repository.weekly = Result.failure(DomainError.Timeout)
            viewModel.onIntent(ScheduleIntent.Refresh)
            advanceUntilIdle()

            assertEquals(content, viewModel.state.value)
            assertEquals(ScheduleEffect.ShowError(networkError), effects.awaitItem())
            effects.expectNoEvents()
        }
    }

    @Test
    fun `screen start reloads only on a new local day, and selects the new today`() = runTest {
        givenAWeek()
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onIntent(ScheduleIntent.DaySelected(DayOfWeek.MONDAY))

        viewModel.onIntent(ScheduleIntent.ScreenStarted)
        advanceUntilIdle()
        assertEquals(2, repository.calls.size)
        assertEquals(DayOfWeek.MONDAY, viewModel.state.value.selectedDay)

        clock.instant += 1.days
        viewModel.onIntent(ScheduleIntent.ScreenStarted)
        advanceUntilIdle()

        assertEquals(
            listOf(Call.Weekly(newYork, forceRefresh = false), Call.Weekly(newYork, forceRefresh = false)),
            weeklyCalls(),
        )
        val reloaded = viewModel.state.value
        assertEquals(DayOfWeek.FRIDAY, reloaded.selectedDay)
        assertEquals(listOf(DayOfWeek.FRIDAY), reloaded.days.filter { it.isToday }.map { it.dayOfWeek })
        assertNotNull(reloaded.day)

        viewModel.onIntent(ScheduleIntent.ScreenStarted)
        advanceUntilIdle()
        assertEquals(2, weeklyCalls().size)
    }

    @Test
    fun `a Sunday week start puts Sunday first`() = runTest {
        settings.settings.value = AppSettings(weekStart = WeekStart.SUNDAY)
        givenAWeek()
        val viewModel = createViewModel()
        advanceUntilIdle()

        val loaded = viewModel.state.value
        assertEquals(DayOfWeek.SUNDAY, loaded.days.first().dayOfWeek)
        assertEquals(DayOfWeek.SATURDAY, loaded.days.last().dayOfWeek)
        assertEquals(DayOfWeek.THURSDAY, loaded.selectedDay)
    }

    @Test
    fun `screen start after a week start change reloads in the new order and keeps the weekday`() = runTest {
        givenAWeek()
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onIntent(ScheduleIntent.DaySelected(DayOfWeek.FRIDAY))

        settings.settings.value = AppSettings(weekStart = WeekStart.SUNDAY)
        viewModel.onIntent(ScheduleIntent.ScreenStarted)
        advanceUntilIdle()

        assertEquals(2, weeklyCalls().size)
        val reloaded = viewModel.state.value
        assertEquals(DayOfWeek.SUNDAY, reloaded.days.first().dayOfWeek)
        assertEquals(DayOfWeek.FRIDAY, reloaded.selectedDay)
        assertEquals(listOf("d", "e"), reloaded.slugs())

        viewModel.onIntent(ScheduleIntent.ScreenStarted)
        advanceUntilIdle()
        assertEquals(2, weeklyCalls().size)
    }

    @Test
    fun `screen start before a successful load does nothing`() = runTest {
        repository.weekly = Result.failure(DomainError.Network)
        val viewModel = createViewModel()
        advanceUntilIdle()

        clock.instant += 1.days
        viewModel.onIntent(ScheduleIntent.ScreenStarted)
        advanceUntilIdle()

        assertEquals(1, weeklyCalls().size)
    }

    @Test
    fun `a second refresh while loading is ignored`() = runTest {
        givenAWeek()
        val viewModel = createViewModel()
        advanceUntilIdle()
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate

        viewModel.onIntent(ScheduleIntent.Refresh)
        runCurrent()
        viewModel.onIntent(ScheduleIntent.Refresh)
        runCurrent()
        assertEquals(2, weeklyCalls().size)

        gate.complete(Unit)
        advanceUntilIdle()

        assertEquals(2, weeklyCalls().size)
        assertFalse(viewModel.state.value.isRefreshing)
    }

    @Test
    fun `a show click opens its details and the settings click opens settings`() = runTest {
        val viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onIntent(ScheduleIntent.ShowClicked("x"))
            assertEquals(ScheduleEffect.OpenDetails("x"), awaitItem())

            viewModel.onIntent(ScheduleIntent.SettingsClicked)
            assertEquals(ScheduleEffect.OpenSettings, awaitItem())
        }
    }
}
