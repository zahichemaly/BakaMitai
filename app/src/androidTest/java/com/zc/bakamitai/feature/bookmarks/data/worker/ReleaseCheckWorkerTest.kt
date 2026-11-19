package com.zc.bakamitai.feature.bookmarks.data.worker

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.zc.bakamitai.common.domain.model.AppSettings
import com.zc.bakamitai.common.domain.model.ScheduledShow
import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.domain.model.ThemeMode
import com.zc.bakamitai.common.domain.model.TimeFormat
import com.zc.bakamitai.common.domain.model.WeekStart
import com.zc.bakamitai.common.domain.repository.SettingsRepository
import com.zc.bakamitai.common.domain.util.TimeZoneProvider
import com.zc.bakamitai.feature.bookmarks.domain.model.AiringSlot
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkToggle
import com.zc.bakamitai.feature.bookmarks.domain.model.EpisodeAlert
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatch
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatchUpdate
import com.zc.bakamitai.feature.bookmarks.domain.repository.BookmarkRepository
import com.zc.bakamitai.feature.bookmarks.domain.repository.ReleaseAlertNotifier
import com.zc.bakamitai.feature.bookmarks.domain.repository.ReleaseCheckScheduler
import com.zc.bakamitai.feature.bookmarks.domain.usecase.ApplyReleaseCheckUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.CheckBookmarkedReleasesUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.GetReleaseAlertsEnabledUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.GetReleaseAlertsStatusUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.ScheduleNextReleaseCheckUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.UpdateAiringSlotsUseCase
import com.zc.bakamitai.feature.home.domain.model.Release
import com.zc.bakamitai.feature.home.domain.repository.BroadcastRepository
import com.zc.bakamitai.feature.home.domain.usecase.GetNewestReleasesUseCase
import com.zc.bakamitai.feature.home.domain.usecase.SearchShowReleasesUseCase
import com.zc.bakamitai.feature.schedule.domain.repository.ScheduleRepository
import com.zc.bakamitai.feature.schedule.domain.usecase.GetWeeklyTemplateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** The worker's wiring, over in-memory fakes: the rules themselves are unit-tested in the domain. */
@RunWith(AndroidJUnit4::class)
class ReleaseCheckWorkerTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val slotTime = Instant.parse("2026-11-16T12:00:00Z")
    private val now = slotTime + 10.minutes
    private val frieren = Show(ShowSlug("frieren"), "Frieren", null)
    private val events = mutableListOf<String>()

    private val repository = InMemoryBookmarkRepository(events)
    private val broadcasts = OneFeedBroadcastRepository()
    private val notifier = RecordingNotifier(events)
    private val scheduler = RecordingScheduler(events)
    private val settings = InMemorySettingsRepository()

    private fun buildWorker(): ReleaseCheckWorker {
        val clock = object : Clock {
            override fun now(): Instant = now
        }
        val timeZoneProvider = object : TimeZoneProvider {
            override fun current(): TimeZone = TimeZone.UTC
        }
        val factory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters,
            ): ListenableWorker = ReleaseCheckWorker(
                context = appContext,
                params = workerParameters,
                getAlertsEnabled = GetReleaseAlertsEnabledUseCase(GetReleaseAlertsStatusUseCase(settings, notifier)),
                checkReleases = CheckBookmarkedReleasesUseCase(
                    repository = repository,
                    updateAiringSlots = UpdateAiringSlotsUseCase(
                        repository = repository,
                        getWeeklyTemplate = GetWeeklyTemplateUseCase(EmptyScheduleRepository(), timeZoneProvider),
                        clock = clock,
                    ),
                    getNewestReleases = GetNewestReleasesUseCase(broadcasts),
                    searchShowReleases = SearchShowReleasesUseCase(broadcasts),
                    clock = clock,
                    timeZoneProvider = timeZoneProvider,
                ),
                applyReleaseCheck = ApplyReleaseCheckUseCase(repository, notifier),
                scheduleNextCheck = ScheduleNextReleaseCheckUseCase(scheduler),
            )
        }
        return TestListenableWorkerBuilder<ReleaseCheckWorker>(context).setWorkerFactory(factory).build()
    }

    private fun awaitedWatch() = ReleaseWatch(
        show = frieren,
        trackingSince = Instant.parse("2026-11-01T00:00:00Z"),
        airing = AiringSlot(DayOfWeek.MONDAY, LocalTime(12, 0), TimeZone.UTC, "Frieren"),
        airingCheckedAt = slotTime,
        lastSeen = null,
        resolvedThrough = Instant.parse("2026-11-09T12:00:00Z"),
    )

    @Test
    fun aNewEpisodeIsSavedThenNotifiedThenTheNextCheckScheduled() = runTest {
        repository.watches = listOf(awaitedWatch())
        broadcasts.newest = listOf(
            Release(frieren, "08", Instant.parse("2026-11-16T11:03:00Z")),
            Release(Show(ShowSlug("other"), "Other", null), "03", Instant.parse("2026-11-15T00:00:00Z")),
        )

        val result = buildWorker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(listOf("save", "notify 08", "next 2026-11-23T12:10:00Z"), events)
    }

    @Test
    fun withAlertsOffNothingIsRequestedOrPosted() = runTest {
        repository.watches = listOf(awaitedWatch())
        notifier.enabled = false

        val result = buildWorker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(0, broadcasts.requests)
        assertEquals(listOf("save", "notify none", "next 2026-11-23T12:10:00Z"), events)
    }

    @Test
    fun withAlertsSwitchedOffInSettingsNothingIsRequestedOrPostedAndTheNextCheckIsScheduled() = runTest {
        repository.watches = listOf(awaitedWatch())
        broadcasts.newest = listOf(Release(frieren, "08", Instant.parse("2026-11-16T11:03:00Z")))
        settings.setNewEpisodeAlerts(false)

        val result = buildWorker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(0, broadcasts.requests)
        assertEquals(listOf("save", "notify none", "next 2026-11-23T12:10:00Z"), events)
    }

    @Test
    fun withoutBookmarksNothingIsScheduled() = runTest {
        val result = buildWorker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(listOf("save", "notify none", "next null"), events)
    }

    private class InMemoryBookmarkRepository(private val events: MutableList<String>) : BookmarkRepository {
        var watches: List<ReleaseWatch> = emptyList()

        override fun observeBookmarks(): Flow<List<Bookmark>> = emptyFlow()

        override suspend fun toggle(bookmark: Bookmark) = Result.success(BookmarkToggle(true, isFirstBookmark = true))

        override suspend fun remove(slug: ShowSlug): Result<Bookmark?> = Result.success(null)

        override suspend fun restore(bookmark: Bookmark, trackingSince: Instant) = Result.success(Unit)

        override suspend fun getReleaseWatches(): Result<List<ReleaseWatch>> = Result.success(watches)

        override suspend fun updateAiringSlots(slots: Map<ShowSlug, AiringSlot?>, checkedAt: Instant) =
            Result.success(Unit)

        override suspend fun saveReleaseWatches(updates: List<ReleaseWatchUpdate>): Result<Unit> {
            events += "save"
            return Result.success(Unit)
        }
    }

    private class OneFeedBroadcastRepository : BroadcastRepository {
        var newest: List<Release> = emptyList()
        var requests = 0

        override suspend fun getTodaySchedule(timeZone: TimeZone): Result<List<ScheduledShow>> =
            Result.success(emptyList())

        override suspend fun getLatestReleases(page: Int, timeZone: TimeZone): Result<List<Release>> =
            Result.success(emptyList())

        override suspend fun searchReleases(query: String, timeZone: TimeZone): Result<List<Release>> {
            requests++
            return Result.success(if (query.isEmpty()) newest else emptyList())
        }
    }

    private class EmptyScheduleRepository : ScheduleRepository {
        override suspend fun getWeeklySchedule(timeZone: TimeZone, forceRefresh: Boolean) =
            Result.success(emptyMap<DayOfWeek, List<ScheduledShow>>())

        override suspend fun getTodaySchedule(timeZone: TimeZone): Result<List<ScheduledShow>> =
            Result.success(emptyList())
    }

    private class RecordingNotifier(private val events: MutableList<String>) : ReleaseAlertNotifier {
        var enabled = true

        override fun areAlertsEnabled(): Boolean = enabled

        override fun notify(alerts: List<EpisodeAlert>) {
            events += if (alerts.isEmpty()) "notify none" else "notify " + alerts.joinToString { it.episode }
        }
    }

    private class InMemorySettingsRepository : SettingsRepository {
        private val settings = MutableStateFlow(AppSettings())

        override fun observeSettings(): Flow<AppSettings> = settings

        override suspend fun setWeekStart(value: WeekStart): Result<Unit> = update { it.copy(weekStart = value) }

        override suspend fun setTimeFormat(value: TimeFormat): Result<Unit> = update { it.copy(timeFormat = value) }

        override suspend fun setThemeMode(value: ThemeMode): Result<Unit> = update { it.copy(themeMode = value) }

        override suspend fun setNewEpisodeAlerts(enabled: Boolean): Result<Unit> =
            update { it.copy(newEpisodeAlerts = enabled) }

        private fun update(transform: (AppSettings) -> AppSettings): Result<Unit> {
            settings.update(transform)
            return Result.success(Unit)
        }
    }

    private class RecordingScheduler(private val events: MutableList<String>) : ReleaseCheckScheduler {
        override fun requestCheck() = Unit

        override fun ensureCheckScheduled() = Unit

        override fun scheduleNextCheck(at: Instant?) {
            events += "next $at"
        }
    }
}
