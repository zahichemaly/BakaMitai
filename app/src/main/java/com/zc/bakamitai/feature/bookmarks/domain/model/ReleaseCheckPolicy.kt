package com.zc.bakamitai.feature.bookmarks.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/** The tunables of new-episode alerts (docs/5-bookmarks.md §5.3–§5.6). */
object ReleaseCheckPolicy {

    /** When to check after a weekly slot, while its episode isn't out: 8 attempts over a day. */
    val RETRY_OFFSETS: List<Duration> =
        listOf(10.minutes, 30.minutes, 1.hours, 2.hours, 4.hours, 8.hours, 16.hours, 24.hours)

    /** A successful check this long after a slot that found nothing resolves the slot silently. */
    val GIVE_UP_AFTER: Duration = 24.hours

    /** SubsPlease's `release_date` can be about an hour early; comparisons with a clock or a slot allow for it. */
    val RELEASE_DATE_TOLERANCE: Duration = 1.hours

    /** Weekly slots older than this are refreshed. */
    val AIRING_MAX_AGE: Duration = 24.hours

    /** Checks due within this window of the earliest one run together, at the latest of them. */
    val COALESCE_WINDOW: Duration = 15.minutes

    /** The retry delay after a failed check past the ladder, or a failed read. */
    val FAILED_CHECK_RETRY: Duration = 1.hours

    /** At most this many per-show searches per check. */
    const val MAX_FALLBACK_SEARCHES: Int = 5

    /** When per-show searches were left over, the next check runs this soon. */
    val FALLBACK_CONTINUE_DELAY: Duration = 15.minutes

    /** How often the Bookmarks screen re-derives its next-airing labels. */
    val LABEL_REFRESH: Duration = 1.minutes
}
