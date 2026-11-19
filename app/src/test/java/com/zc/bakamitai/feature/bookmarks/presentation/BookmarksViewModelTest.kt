package com.zc.bakamitai.feature.bookmarks.presentation

import app.cash.turbine.test
import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.repository.FakeSettingsRepository
import com.zc.bakamitai.common.domain.util.FixedTimeZoneProvider
import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.util.FixedClock
import com.zc.bakamitai.common.util.MainDispatcherRule
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.bookmark
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.watch
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkSort
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseAlertsStatus
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeBookmarkRepository
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeBookmarkSortRepository
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseAlertNotifier
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseCheckScheduler
import com.zc.bakamitai.feature.bookmarks.domain.repository.FakeReleaseCheckScheduler.Call
import com.zc.bakamitai.feature.bookmarks.domain.usecase.GetReleaseAlertsStatusUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.ObserveBookmarkSortUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.ObserveBookmarksUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.RefreshAiringSlotsUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.RemoveBookmarkUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.RestoreBookmarkUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.UpdateAiringSlotsUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.UpdateBookmarkSortUseCase
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.mapper.sortChipUIModels
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.mapper.toAlertsChipUIModel
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.mapper.toBookmarkCountLabel
import com.zc.bakamitai.feature.bookmarks.presentation.model.BookmarkSortOption
import com.zc.bakamitai.feature.schedule.domain.repository.FakeScheduleRepository
import com.zc.bakamitai.feature.schedule.domain.usecase.GetWeeklyTemplateUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import org.junit.Rule
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * The labels re-derive every minute (an endless ticker), so these tests run the current tasks (`runCurrent`)
 * rather than advancing until idle.
 */
class BookmarksViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = FixedClock(Instant.parse("2026-11-19T10:00:00Z"))
    private val timeZoneProvider = FixedTimeZoneProvider(TimeZone.UTC)
    private val repository = FakeBookmarkRepository()
    private val schedule = FakeScheduleRepository()
    private val scheduler = FakeReleaseCheckScheduler()
    private val notifier = FakeReleaseAlertNotifier()
    private val settings = FakeSettingsRepository()
    private val sortRepository = FakeBookmarkSortRepository()

    private val frieren = bookmark("frieren", "2026-11-18T09:00:00Z", title = "Sousou no Frieren")
    private val bleach = bookmark("bleach", "2026-11-10T09:00:00Z", title = "Bleach")

    private val genericError = ErrorUIModel(UiText.StringResource(StringResource.error_generic), isRetryable = true)
    private val networkError = ErrorUIModel(UiText.StringResource(StringResource.error_network), isRetryable = true)

    private fun createViewModel() = BookmarksViewModel(
        observeBookmarks = ObserveBookmarksUseCase(repository, clock, timeZoneProvider),
        observeBookmarkSort = ObserveBookmarkSortUseCase(sortRepository),
        updateBookmarkSort = UpdateBookmarkSortUseCase(sortRepository),
        refreshAiringSlots = RefreshAiringSlotsUseCase(
            updateAiringSlots = UpdateAiringSlotsUseCase(
                repository = repository,
                getWeeklyTemplate = GetWeeklyTemplateUseCase(schedule, timeZoneProvider),
                clock = clock,
            ),
            scheduler = scheduler,
        ),
        removeBookmark = RemoveBookmarkUseCase(repository, scheduler),
        restoreBookmark = RestoreBookmarkUseCase(repository, scheduler, clock),
        getAlertsStatus = GetReleaseAlertsStatusUseCase(settings, notifier),
    )

    /** Keeps the `WhileSubscribed` state collected and runs the first load. */
    private fun TestScope.loadedViewModel(): BookmarksViewModel = createViewModel().also { viewModel ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        runCurrent()
    }

    private fun BookmarksViewModel.slugs() = state.value.cards.map { it.slug }

    @Test
    fun `starts loading, then shows the bookmarks newest first with the header`() = runTest {
        repository.bookmarks.value = listOf(bleach, frieren)
        val viewModel = createViewModel()
        assertTrue(viewModel.state.value.isLoading)

        loadedViewModel().let { loaded ->
            val state = loaded.state.value
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertEquals(listOf("frieren", "bleach"), loaded.slugs())
            assertEquals(sortChipUIModels(), state.header.sortChips)
            assertEquals(2.toBookmarkCountLabel(), state.header.countLabel)
            assertEquals(ReleaseAlertsStatus.ON.toAlertsChipUIModel(), state.header.alertsChip)
            assertFalse(state.isEmpty)
        }
    }

    @Test
    fun `no bookmarks is the empty state`() = runTest {
        val viewModel = loadedViewModel()

        assertTrue(viewModel.state.value.isEmpty)
    }

    @Test
    fun `a failed read shows the error, and Retry reads again`() = runTest {
        repository.bookmarks.value = listOf(frieren)
        repository.observeFailure = DomainError.Unknown(IOException("corrupt"))
        val viewModel = loadedViewModel()
        assertEquals(genericError, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)

        repository.observeFailure = null
        viewModel.onIntent(BookmarksIntent.Retry)
        runCurrent()

        assertNull(viewModel.state.value.error)
        assertEquals(listOf("frieren"), viewModel.slugs())
    }

    @Test
    fun `selecting a sort saves it and re-sorts the grid`() = runTest {
        repository.bookmarks.value = listOf(frieren, bleach)
        val viewModel = loadedViewModel()

        viewModel.onIntent(BookmarksIntent.SortSelected(BookmarkSortOption.TITLE))
        runCurrent()

        assertEquals(BookmarkSort.TITLE, sortRepository.sort.value)
        assertEquals(BookmarkSortOption.TITLE, viewModel.state.value.selectedSort)
        assertEquals(listOf("bleach", "frieren"), viewModel.slugs())
    }

    @Test
    fun `the saved sort is the initial sort`() = runTest {
        sortRepository.sort.value = BookmarkSort.TITLE
        repository.bookmarks.value = listOf(frieren, bleach)
        val viewModel = loadedViewModel()

        assertEquals(BookmarkSortOption.TITLE, viewModel.state.value.selectedSort)
        assertEquals(listOf("bleach", "frieren"), viewModel.slugs())
    }

    @Test
    fun `a failed sort write shows the error and keeps the saved sort`() = runTest {
        repository.bookmarks.value = listOf(frieren, bleach)
        sortRepository.failWrites = true
        val viewModel = loadedViewModel()

        viewModel.effect.test {
            viewModel.onIntent(BookmarksIntent.SortSelected(BookmarkSortOption.TITLE))
            assertEquals(BookmarksEffect.ShowError(genericError), awaitItem())
        }
        assertEquals(BookmarkSortOption.RECENTLY_ADDED, viewModel.state.value.selectedSort)
        assertEquals(listOf("frieren", "bleach"), viewModel.slugs())
    }

    @Test
    fun `removing shows the snackbar, and Undo restores the same bookmark`() = runTest {
        repository.bookmarks.value = listOf(frieren, bleach)
        val viewModel = loadedViewModel()

        viewModel.effect.test {
            viewModel.onIntent(BookmarksIntent.RemoveClicked("frieren"))
            assertEquals(
                BookmarksEffect.ShowRemoved(
                    UiText.StringResource(StringResource.bookmarks_removed, listOf("Sousou no Frieren")),
                    "frieren",
                ),
                awaitItem(),
            )
        }
        runCurrent()
        assertEquals(listOf("bleach"), viewModel.slugs())

        viewModel.onIntent(BookmarksIntent.UndoRemoveClicked("frieren"))
        runCurrent()

        assertEquals(listOf(frieren to clock.instant), repository.restored)
        assertEquals(listOf("frieren", "bleach"), viewModel.slugs())
    }

    @Test
    fun `an Undo for another slug, or a second Undo, is ignored`() = runTest {
        repository.bookmarks.value = listOf(frieren, bleach)
        val viewModel = loadedViewModel()

        viewModel.onIntent(BookmarksIntent.RemoveClicked("frieren"))
        runCurrent()
        viewModel.onIntent(BookmarksIntent.UndoRemoveClicked("bleach"))
        viewModel.onIntent(BookmarksIntent.UndoRemoveClicked("frieren"))
        viewModel.onIntent(BookmarksIntent.UndoRemoveClicked("frieren"))
        runCurrent()

        assertEquals(listOf(frieren to clock.instant), repository.restored)
    }

    @Test
    fun `a failed removal shows the error`() = runTest {
        repository.bookmarks.value = listOf(frieren)
        repository.removeFailure = DomainError.Unknown(IOException("disk"))
        val viewModel = loadedViewModel()

        viewModel.effect.test {
            viewModel.onIntent(BookmarksIntent.RemoveClicked("frieren"))
            assertEquals(BookmarksEffect.ShowError(genericError), awaitItem())
        }
    }

    @Test
    fun `screen start re-reads the alerts and makes sure a check is scheduled`() = runTest {
        val viewModel = loadedViewModel()
        assertEquals(listOf<Call>(Call.EnsureScheduled), scheduler.calls)

        notifier.enabled = false
        viewModel.onIntent(BookmarksIntent.ScreenStarted)
        runCurrent()

        assertEquals(ReleaseAlertsStatus.BLOCKED.toAlertsChipUIModel(), viewModel.state.value.header.alertsChip)
        assertEquals(listOf<Call>(Call.EnsureScheduled, Call.EnsureScheduled), scheduler.calls)
    }

    @Test
    fun `pull-to-refresh forces the schedule and shows refreshing until it's done`() = runTest {
        repository.bookmarks.value = listOf(frieren)
        repository.watches = listOf(watch(slug = "frieren", airingCheckedAt = "2026-11-19T09:00:00Z"))
        val viewModel = loadedViewModel()
        val gate = CompletableDeferred<Unit>()
        schedule.gate = gate

        viewModel.onIntent(BookmarksIntent.Refresh)
        runCurrent()
        assertTrue(viewModel.state.value.isRefreshing)
        viewModel.onIntent(BookmarksIntent.Refresh)

        gate.complete(Unit)
        runCurrent()
        assertFalse(viewModel.state.value.isRefreshing)
        assertEquals(1, schedule.calls.size)
        assertTrue(schedule.calls.single().let { it is FakeScheduleRepository.Call.Weekly && it.forceRefresh })
    }

    @Test
    fun `a failed refresh shows the error and keeps the grid`() = runTest {
        repository.bookmarks.value = listOf(frieren)
        repository.watches = listOf(watch(slug = "frieren"))
        schedule.weekly = Result.failure(DomainError.Network)
        val viewModel = loadedViewModel()

        viewModel.effect.test {
            viewModel.onIntent(BookmarksIntent.Refresh)
            assertEquals(BookmarksEffect.ShowError(networkError), awaitItem())
        }
        assertFalse(viewModel.state.value.isRefreshing)
        assertEquals(listOf("frieren"), viewModel.slugs())
    }

    @Test
    fun `the alerts chip opens Settings when on or switched off, and turns alerts on when blocked`() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effect.test {
            viewModel.onIntent(BookmarksIntent.AlertsChipClicked)
            assertEquals(BookmarksEffect.OpenSettings, awaitItem())

            settings.settings.value = AppSettings(newEpisodeAlerts = false)
            viewModel.onIntent(BookmarksIntent.ScreenStarted)
            runCurrent()
            assertEquals(ReleaseAlertsStatus.OFF.toAlertsChipUIModel(), viewModel.state.value.header.alertsChip)
            viewModel.onIntent(BookmarksIntent.AlertsChipClicked)
            assertEquals(BookmarksEffect.OpenSettings, awaitItem())

            settings.settings.value = AppSettings()
            notifier.enabled = false
            viewModel.onIntent(BookmarksIntent.ScreenStarted)
            runCurrent()
            viewModel.onIntent(BookmarksIntent.AlertsChipClicked)
            assertEquals(BookmarksEffect.EnableAlerts, awaitItem())
        }
    }

    @Test
    fun `a denied permission keeps alerts off and offers the settings`() = runTest {
        notifier.enabled = false
        val viewModel = loadedViewModel()

        viewModel.effect.test {
            viewModel.onIntent(BookmarksIntent.AlertsPermissionResult(granted = false))
            assertEquals(BookmarksEffect.ShowAlertsDenied, awaitItem())
        }
        assertEquals(ReleaseAlertsStatus.BLOCKED.toAlertsChipUIModel(), viewModel.state.value.header.alertsChip)
    }

    @Test
    fun `a granted permission turns the chip on`() = runTest {
        notifier.enabled = false
        val viewModel = loadedViewModel()

        notifier.enabled = true
        viewModel.onIntent(BookmarksIntent.AlertsPermissionResult(granted = true))
        runCurrent()

        assertEquals(ReleaseAlertsStatus.ON.toAlertsChipUIModel(), viewModel.state.value.header.alertsChip)
    }

    @Test
    fun `navigation intents become effects`() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effect.test {
            viewModel.onIntent(BookmarksIntent.BookmarkClicked("frieren"))
            assertEquals(BookmarksEffect.OpenDetails("frieren"), awaitItem())
            viewModel.onIntent(BookmarksIntent.DiscoverShowsClicked)
            assertEquals(BookmarksEffect.OpenSchedule, awaitItem())
            viewModel.onIntent(BookmarksIntent.SettingsClicked)
            assertEquals(BookmarksEffect.OpenSettings, awaitItem())
        }
    }
}
