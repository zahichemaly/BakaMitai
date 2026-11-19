package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.util.TimeZoneProvider
import com.zc.bakamitai.feature.bookmarks.domain.model.AiringSlot
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseCheckOutcome
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseCheckPolicy.COALESCE_WINDOW
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseCheckPolicy.FAILED_CHECK_RETRY
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseCheckPolicy.FALLBACK_CONTINUE_DELAY
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseCheckPolicy.MAX_FALLBACK_SEARCHES
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseCheckPolicy.RELEASE_DATE_TOLERANCE
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatch
import com.zc.bakamitai.feature.bookmarks.domain.repository.BookmarkRepository
import com.zc.bakamitai.feature.home.domain.model.Release
import com.zc.bakamitai.feature.home.domain.usecase.GetNewestReleasesUseCase
import com.zc.bakamitai.feature.home.domain.usecase.SearchShowReleasesUseCase
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * One background release check for every bookmark (docs/5-bookmarks.md §5.3): refreshes stale slots, and only
 * when an episode is awaited, fetches the newest releases once for all bookmarks (plus a few per-show searches
 * when they don't reach back to a slot), decides what's new, and when to run again. It never saves or posts
 * anything (see [ApplyReleaseCheckUseCase]) and never fails: network and disk failures only shape
 * [ReleaseCheckOutcome.nextCheckAt]. Only cancellation propagates.
 */
@Factory
class CheckBookmarkedReleasesUseCase(
    private val repository: BookmarkRepository,
    private val updateAiringSlots: UpdateAiringSlotsUseCase,
    private val getNewestReleases: GetNewestReleasesUseCase,
    private val searchShowReleases: SearchShowReleasesUseCase,
    private val clock: Clock,
    private val timeZoneProvider: TimeZoneProvider,
) {

    /** @param alertsEnabled when `false`, nothing is requested: awaited episodes are resolved silently. */
    suspend operator fun invoke(alertsEnabled: Boolean): ReleaseCheckOutcome {
        val now = clock.now()
        val stored = repository.getReleaseWatches().getOrNull()
        return when {
            stored == null -> ReleaseCheckOutcome(emptyList(), emptyList(), nextCheckAt = now + FAILED_CHECK_RETRY)
            stored.isEmpty() -> ReleaseCheckOutcome(emptyList(), emptyList(), nextCheckAt = null)
            !alertsEnabled -> resolveSilently(stored, now)
            else -> check(stored, now)
        }
    }

    /** Refreshes stale slots, then checks the releases only when an episode is awaited. */
    private suspend fun check(stored: List<ReleaseWatch>, now: Instant): ReleaseCheckOutcome {
        val slotRefresh = updateAiringSlots(forceRefresh = false)
        val watches = if (slotRefresh.getOrDefault(false)) {
            repository.getReleaseWatches().getOrDefault(stored)
        } else {
            stored
        }
        // A bookmark whose slot was never fetched has no due time yet: retry the schedule later.
        val slotRetry = (now + FAILED_CHECK_RETRY)
            .takeIf { slotRefresh.isFailure && watches.any { it.airingCheckedAt == null } }

        val pending = watches.mapNotNull { watch ->
            val slot = watch.airing ?: return@mapNotNull null
            watch.pendingOccurrence(now)?.let { PendingWatch(watch, slot, it) }
        }
        return if (pending.isEmpty()) {
            unchanged(watches, now, listOfNotNull(slotRetry))
        } else {
            checkReleases(watches, pending, now, listOfNotNull(slotRetry))
        }
    }

    private suspend fun checkReleases(
        watches: List<ReleaseWatch>,
        pending: List<PendingWatch>,
        now: Instant,
        extraDueTimes: List<Instant>,
    ): ReleaseCheckOutcome {
        val timeZone = timeZoneProvider.current()
        val sweep = getNewestReleases(timeZone).getOrElse { return unchanged(watches, now, extraDueTimes) }
        val uncovered = pending.filter { it.isUncoveredBy(sweep) }.sortedBy { it.occurrence }
        val searched = uncovered.take(MAX_FALLBACK_SEARCHES).associate { pendingWatch ->
            val slug = pendingWatch.watch.show.slug
            slug to searchShowReleases(slug, pendingWatch.slot.releaseTitle, timeZone).getOrNull()
        }
        val uncoveredSlugs = uncovered.mapTo(mutableSetOf()) { it.watch.show.slug }

        val results = watches.map { watch ->
            val slug = watch.show.slug
            val searchedReleases = searched[slug]
            val candidates = (sweep.filter { it.show.slug == slug } + searchedReleases.orEmpty()).distinct()
            watch.evaluate(candidates, now, coversPending = slug !in uncoveredSlugs || searchedReleases != null)
        }
        val continuation = (now + FALLBACK_CONTINUE_DELAY).takeIf { uncovered.size > searched.size }
        return ReleaseCheckOutcome(
            updates = watches.zip(results)
                .filter { (watch, result) -> result.watch != watch }
                .map { (_, result) -> result.watch.toUpdate() },
            alerts = results.mapNotNull { it.alert },
            nextCheckAt = coalesce(results.mapNotNull { it.watch.nextCheckAt(now) } + extraDueTimes + continuation),
        )
    }

    /** Alerts are off: no requests, every awaited episode is given up, and the checks follow the slots. */
    private fun resolveSilently(watches: List<ReleaseWatch>, now: Instant): ReleaseCheckOutcome {
        val updated = watches.map { watch -> watch.pendingOccurrence(now)?.let(watch::resolve) ?: watch }
        return ReleaseCheckOutcome(
            updates = watches.zip(updated).filter { (old, new) -> old != new }.map { (_, new) -> new.toUpdate() },
            alerts = emptyList(),
            nextCheckAt = coalesce(updated.mapNotNull { it.nextCheckAt(now) }),
        )
    }

    /** Nothing to save: nothing was awaited, or the releases couldn't be fetched (the ladder goes on). */
    private fun unchanged(watches: List<ReleaseWatch>, now: Instant, extraDueTimes: List<Instant>) =
        ReleaseCheckOutcome(
            updates = emptyList(),
            alerts = emptyList(),
            nextCheckAt = coalesce(watches.mapNotNull { it.nextCheckAt(now) } + extraDueTimes),
        )

    /**
     * Whether the newest releases may have missed this show's awaited episode: they don't reach back to its slot
     * (allowing for `release_date` being early) and hold nothing new for it.
     */
    private fun PendingWatch.isUncoveredBy(sweep: List<Release>): Boolean {
        val windowStart = sweep.minOfOrNull { it.releasedAt } ?: return true
        return windowStart > occurrence - RELEASE_DATE_TOLERANCE &&
            sweep.none { it.show.slug == watch.show.slug && watch.isNew(it) }
    }

    /** The earliest due time, pushed to the latest one within [COALESCE_WINDOW] of it, so they share a run. */
    private fun coalesce(dueTimes: List<Instant?>): Instant? {
        val times = dueTimes.filterNotNull()
        val earliest = times.minOrNull() ?: return null
        return times.filter { it <= earliest + COALESCE_WINDOW }.max()
    }

    private data class PendingWatch(val watch: ReleaseWatch, val slot: AiringSlot, val occurrence: Instant)
}
