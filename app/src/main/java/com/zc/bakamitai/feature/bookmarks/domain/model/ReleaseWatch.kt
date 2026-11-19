package com.zc.bakamitai.feature.bookmarks.domain.model

import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseCheckPolicy.AIRING_MAX_AGE
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseCheckPolicy.FAILED_CHECK_RETRY
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseCheckPolicy.GIVE_UP_AFTER
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseCheckPolicy.RELEASE_DATE_TOLERANCE
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseCheckPolicy.RETRY_OFFSETS
import com.zc.bakamitai.feature.home.domain.model.Release
import kotlin.time.Instant

/**
 * One bookmark's new-episode tracking, and the rules that decide when to check it and what is new
 * (docs/5-bookmarks.md §5.3–§5.4).
 *
 * @property trackingSince releases before it (allowing for `release_date` being early) never notify.
 * @property airing the weekly slot, or `null` when the show isn't on the weekly schedule: it's then only checked
 * when another show's check happens to see its releases.
 * @property airingCheckedAt when [airing] was last refreshed; `null` = never.
 * @property lastSeen the newest release seen so far.
 * @property resolvedThrough the latest weekly occurrence already handled (released, given up, or before tracking);
 * `null` = a day before [trackingSince], so a show bookmarked just after its slot is still checked for it.
 */
data class ReleaseWatch(
    val show: Show,
    val trackingSince: Instant,
    val airing: AiringSlot?,
    val airingCheckedAt: Instant?,
    val lastSeen: SeenEpisode?,
    val resolvedThrough: Instant?,
) {

    private val handledThrough: Instant get() = resolvedThrough ?: (trackingSince - GIVE_UP_AFTER)

    /** The latest slot occurrence at or before [now] that isn't handled yet: its episode is awaited. */
    fun pendingOccurrence(now: Instant): Instant? = airing?.latestAtOrBefore(now)?.takeIf { it > handledThrough }

    /** Whether the weekly slot was never fetched, or is older than a day. */
    fun isAiringStale(now: Instant): Boolean = airingCheckedAt == null || now - airingCheckedAt >= AIRING_MAX_AGE

    /**
     * Whether [release] of this show is a new episode: not a batch, released after tracking began, and later than
     * [lastSeen] with another key and not a lower number (a revision or an older episode released late isn't new).
     */
    fun isNew(release: Release): Boolean =
        !release.episode.isBatchRange() && release.isAfterTracking() && lastSeen.isBefore(release)

    /**
     * This bookmark after a successful check that found [candidates] (its releases, in any order):
     * - every release that advances [lastSeen] does so, in release order; those after tracking are new, the
     *   others are a silent baseline;
     * - the newest of them resolves the slot occurrence nearest to its real release time;
     * - a still pending occurrence is resolved silently once a day has passed, when [coversPending] (the
     *   candidates reach back to it);
     * - the alert names the newest new episode and how many were new.
     */
    fun evaluate(candidates: List<Release>, now: Instant, coversPending: Boolean = true): ReleaseWatchResult {
        var seen = lastSeen
        var newest: Release? = null
        val newReleases = mutableListOf<Release>()
        candidates
            .filterNot { it.episode.isBatchRange() }
            .sortedBy { it.releasedAt }
            .forEach { release ->
                if (seen.isBefore(release)) {
                    if (release.isAfterTracking()) newReleases += release
                    seen = SeenEpisode(EpisodeKey.of(release.episode), release.releasedAt)
                    newest = release
                }
            }
        var updated = copy(lastSeen = seen)
        val releasedOccurrence = newest?.let { airing?.nearestTo(it.releasedAt + RELEASE_DATE_TOLERANCE) }
        if (releasedOccurrence != null) updated = updated.resolve(releasedOccurrence)
        val pending = updated.pendingOccurrence(now)
        if (pending != null && coversPending && now >= pending + GIVE_UP_AFTER) updated = updated.resolve(pending)
        val alert = newReleases.lastOrNull()?.let { release ->
            EpisodeAlert(
                show = show,
                episode = release.episode,
                releasedAt = release.releasedAt,
                newEpisodeCount = newReleases.size,
            )
        }
        return ReleaseWatchResult(watch = updated, alert = alert)
    }

    /** This bookmark with [occurrence] handled; an earlier one never moves [resolvedThrough] back. */
    fun resolve(occurrence: Instant): ReleaseWatch =
        if (resolvedThrough != null && resolvedThrough >= occurrence) this else copy(resolvedThrough = occurrence)

    /**
     * When this bookmark needs a check: the next step of the retry ladder while an episode is awaited (an hour
     * from [now] once past the ladder, after failed checks), otherwise 10 minutes after the next slot.
     * `null` without a slot.
     */
    fun nextCheckAt(now: Instant): Instant? {
        val slot = airing ?: return null
        val pending = pendingOccurrence(now)
        return if (pending != null) {
            RETRY_OFFSETS.map { pending + it }.firstOrNull { it > now } ?: (now + FAILED_CHECK_RETRY)
        } else {
            slot.firstAfter(maxOf(now, handledThrough)) + RETRY_OFFSETS.first()
        }
    }

    /** The state a check saves. */
    fun toUpdate(): ReleaseWatchUpdate =
        ReleaseWatchUpdate(slug = show.slug, lastSeen = lastSeen, resolvedThrough = resolvedThrough)

    private fun Release.isAfterTracking(): Boolean = releasedAt + RELEASE_DATE_TOLERANCE >= trackingSince
}

/** Whether [release] comes after this seen episode: later, another key, and not a lower number. */
private fun SeenEpisode?.isBefore(release: Release): Boolean {
    if (this == null) return true
    val releaseKey = EpisodeKey.of(release.episode)
    val releaseNumber = releaseKey.number
    val seenNumber = key.number
    return release.releasedAt > releasedAt &&
        releaseKey != key &&
        (releaseNumber == null || seenNumber == null || releaseNumber > seenNumber)
}
