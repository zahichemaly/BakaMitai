package com.zc.bakamitai.feature.bookmarks.domain.model

import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.MONDAY_NOON
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.seen
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.watch
import com.zc.bakamitai.feature.home.domain.HomeTestData.release
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** The rules of docs/5-bookmarks.md §5.3–§5.4, around one Monday 12:00 UTC slot (E = 16 Nov 2026 12:00). */
class ReleaseWatchTest {

    private val nextMondayCheck = Instant.parse("2026-11-23T12:10:00Z")

    // region pending occurrence

    @Test
    fun `bookmarked two hours after the slot, the slot is still awaited`() {
        val watch = watch(trackingSince = "2026-11-16T14:00:00Z")

        assertEquals(MONDAY_NOON, watch.pendingOccurrence(Instant.parse("2026-11-16T14:10:00Z")))
    }

    @Test
    fun `bookmarked two days after the slot, nothing is awaited until the next one`() {
        val watch = watch(trackingSince = "2026-11-18T12:00:00Z")
        val now = Instant.parse("2026-11-18T12:10:00Z")

        assertNull(watch.pendingOccurrence(now))
        assertEquals(nextMondayCheck, watch.nextCheckAt(now))
    }

    @Test
    fun `before the slot nothing is awaited and the check is due 10 minutes after it`() {
        val watch = watch(trackingSince = "2026-11-15T09:00:00Z")
        val now = Instant.parse("2026-11-16T11:00:00Z")

        assertNull(watch.pendingOccurrence(now))
        assertEquals(MONDAY_NOON + 10.minutes, watch.nextCheckAt(now))
    }

    @Test
    fun `a resolved slot waits for the next week`() {
        val watch = watch(resolvedThrough = "2026-11-16T12:00:00Z")

        assertNull(watch.pendingOccurrence(MONDAY_NOON + 1.hours))
        assertEquals(nextMondayCheck, watch.nextCheckAt(MONDAY_NOON + 1.hours))
    }

    @Test
    fun `a show that isn't on the weekly schedule is never due`() {
        val watch = watch(airing = null)

        assertNull(watch.pendingOccurrence(MONDAY_NOON))
        assertNull(watch.nextCheckAt(MONDAY_NOON))
    }

    // endregion

    // region retry ladder and give-up

    @Test
    fun `while awaited, checks follow the ladder from the slot`() {
        val watch = watch()

        assertEquals(MONDAY_NOON + 10.minutes, watch.nextCheckAt(MONDAY_NOON + 5.minutes))
        assertEquals(MONDAY_NOON + 30.minutes, watch.nextCheckAt(MONDAY_NOON + 10.minutes))
        assertEquals(MONDAY_NOON + 1.hours, watch.nextCheckAt(MONDAY_NOON + 31.minutes))
        assertEquals(MONDAY_NOON + 2.hours, watch.nextCheckAt(MONDAY_NOON + 1.hours))
        assertEquals(MONDAY_NOON + 4.hours, watch.nextCheckAt(MONDAY_NOON + 3.hours))
        assertEquals(MONDAY_NOON + 8.hours, watch.nextCheckAt(MONDAY_NOON + 4.hours))
        assertEquals(MONDAY_NOON + 16.hours, watch.nextCheckAt(MONDAY_NOON + 9.hours))
        assertEquals(MONDAY_NOON + 24.hours, watch.nextCheckAt(MONDAY_NOON + 23.hours))
    }

    @Test
    fun `past the ladder after failed checks, the next check is an hour away`() {
        val now = MONDAY_NOON + 25.hours

        assertEquals(now + 1.hours, watch().nextCheckAt(now))
    }

    @Test
    fun `a successful check finds nothing before a day has passed, so the slot stays awaited`() {
        val result = watch().evaluate(emptyList(), now = MONDAY_NOON + 23.hours)

        assertEquals(MONDAY_NOON, result.watch.pendingOccurrence(MONDAY_NOON + 23.hours))
        assertNull(result.alert)
    }

    @Test
    fun `a successful check a day after the slot that finds nothing gives up silently`() {
        val now = MONDAY_NOON + 24.hours
        val result = watch().evaluate(emptyList(), now)

        assertEquals(MONDAY_NOON, result.watch.resolvedThrough)
        assertNull(result.watch.pendingOccurrence(now))
        assertNull(result.alert)
        assertEquals(nextMondayCheck, result.watch.nextCheckAt(now))
    }

    @Test
    fun `candidates that don't reach back to the slot never give it up`() {
        val now = MONDAY_NOON + 25.hours
        val result = watch().evaluate(emptyList(), now, coversPending = false)

        assertEquals(MONDAY_NOON, result.watch.pendingOccurrence(now))
    }

    // endregion

    // region what is new

    @Test
    fun `a release before tracking only sets the baseline and settles its slot`() {
        // Bookmarked at 14:00; the episode came out at 12:05 (release_date about an hour early).
        val watch = watch(trackingSince = "2026-11-16T14:00:00Z")
        val now = Instant.parse("2026-11-16T14:10:00Z")

        val result = watch.evaluate(listOf(release("frieren", "08", "2026-11-16T11:05:00Z")), now)

        assertNull(result.alert)
        assertEquals(seen("08", "2026-11-16T11:05:00Z"), result.watch.lastSeen)
        assertEquals(MONDAY_NOON, result.watch.resolvedThrough)
        assertNull(result.watch.pendingOccurrence(now))
    }

    @Test
    fun `a release 30 minutes after tracking is new despite the early release_date`() {
        // Bookmarked at 12:30; released at 13:00, dated 12:00.
        val watch = watch(trackingSince = "2026-11-16T12:30:00Z")
        val candidate = release("frieren", "08", "2026-11-16T12:00:00Z")

        val result = watch.evaluate(listOf(candidate), now = Instant.parse("2026-11-16T13:10:00Z"))

        assertEquals(EpisodeAlert(show("frieren"), "08", candidate.releasedAt, newEpisodeCount = 1), result.alert)
        assertEquals(MONDAY_NOON, result.watch.resolvedThrough)
        assertTrue(watch.isNew(candidate))
    }

    @Test
    fun `the expected weekly episode is new and resolves its slot`() {
        val watch = watch(lastSeen = seen("07", "2026-11-09T11:02:00Z"), resolvedThrough = "2026-11-09T12:00:00Z")
        val candidate = release("frieren", "08", "2026-11-16T11:03:00Z")

        val result = watch.evaluate(listOf(candidate), now = MONDAY_NOON + 10.minutes)

        assertEquals("08", result.alert?.episode)
        assertEquals(seen("08", "2026-11-16T11:03:00Z"), result.watch.lastSeen)
        assertEquals(MONDAY_NOON, result.watch.resolvedThrough)
        assertEquals(nextMondayCheck, result.watch.nextCheckAt(MONDAY_NOON + 10.minutes))
    }

    @Test
    fun `a revision of the last episode is never new`() {
        val watch = watch(lastSeen = seen("1216", "2026-11-16T11:00:00Z"))
        val revision = release("frieren", "1216v2", "2026-11-16T15:00:00Z")

        val result = watch.evaluate(listOf(revision), now = MONDAY_NOON + 4.hours)

        assertFalse(watch.isNew(revision))
        assertNull(result.alert)
        assertEquals(seen("1216", "2026-11-16T11:00:00Z"), result.watch.lastSeen)
    }

    @Test
    fun `an older episode released after a newer one is never new`() {
        // Gensou Suikoden released 02 after 06.
        val watch = watch(lastSeen = seen("06", "2026-11-16T11:00:00Z"))
        val older = release("frieren", "02", "2026-11-16T13:00:00Z")

        assertFalse(watch.isNew(older))
        assertNull(watch.evaluate(listOf(older), now = MONDAY_NOON + 2.hours).alert)
    }

    @Test
    fun `specials and movies are new when later and different`() {
        val watch = watch(lastSeen = seen("08", "2026-11-16T11:00:00Z"))

        assertTrue(watch.isNew(release("frieren", "Egghead SP11", "2026-11-17T11:00:00Z")))
        assertTrue(watch.isNew(release("frieren", "Movie", "2026-11-17T11:00:00Z")))
        assertFalse(watch.isNew(release("frieren", "Movie", "2026-11-15T11:00:00Z")))
    }

    @Test
    fun `an episode after a special is new`() {
        val watch = watch(lastSeen = seen("Egghead SP11", "2026-11-10T11:00:00Z"))

        assertTrue(watch.isNew(release("frieren", "08", "2026-11-16T11:00:00Z")))
    }

    @Test
    fun `batches are ignored`() {
        val watch = watch(lastSeen = seen("12", "2026-11-09T11:00:00Z"))
        val batch = release("frieren", "01-12", "2026-11-16T11:00:00Z")

        val result = watch.evaluate(listOf(batch), now = MONDAY_NOON + 1.hours)

        assertFalse(watch.isNew(batch))
        assertNull(result.alert)
        assertEquals(watch.lastSeen, result.watch.lastSeen)
    }

    @Test
    fun `a backlog is one alert with the newest episode and the count`() {
        val watch = watch(lastSeen = seen("06", "2026-11-02T11:00:00Z"))
        val candidates = listOf(
            release("frieren", "08", "2026-11-16T11:00:00Z"),
            release("frieren", "06", "2026-11-02T11:00:00Z"),
            release("frieren", "07", "2026-11-09T11:00:00Z"),
            release("frieren", "07v2", "2026-11-10T11:00:00Z"),
        )

        val result = watch.evaluate(candidates, now = MONDAY_NOON + 1.hours)

        assertEquals(EpisodeAlert(show("frieren"), "08", candidates[0].releasedAt, newEpisodeCount = 2), result.alert)
        assertEquals(seen("08", "2026-11-16T11:00:00Z"), result.watch.lastSeen)
    }

    @Test
    fun `releases from before tracking never notify, even after a baseline`() {
        val watch = watch(trackingSince = "2026-11-16T14:00:00Z")
        val candidates = listOf(
            release("frieren", "07", "2026-11-09T11:00:00Z"),
            release("frieren", "08", "2026-11-16T11:00:00Z"),
        )

        val result = watch.evaluate(candidates, now = Instant.parse("2026-11-16T14:10:00Z"))

        assertNull(result.alert)
        assertEquals(seen("08", "2026-11-16T11:00:00Z"), result.watch.lastSeen)
    }

    // endregion

    // region nearest-occurrence resolution

    @Test
    fun `an early release settles the upcoming slot`() {
        // Released on Sunday, a day before its Monday slot.
        val watch = watch(lastSeen = seen("07", "2026-11-09T11:00:00Z"), resolvedThrough = "2026-11-09T12:00:00Z")
        val now = Instant.parse("2026-11-15T11:30:00Z")

        val result = watch.evaluate(listOf(release("frieren", "08", "2026-11-15T09:55:00Z")), now)

        assertEquals("08", result.alert?.episode)
        assertEquals(MONDAY_NOON, result.watch.resolvedThrough)
        assertEquals(nextMondayCheck, result.watch.nextCheckAt(now))
    }

    @Test
    fun `a release a day late settles the slot it belongs to`() {
        val watch = watch(lastSeen = seen("07", "2026-11-09T11:00:00Z"), resolvedThrough = "2026-11-09T12:00:00Z")
        val now = Instant.parse("2026-11-17T16:00:00Z")

        val result = watch.evaluate(listOf(release("frieren", "08", "2026-11-17T14:00:00Z")), now)

        assertEquals("08", result.alert?.episode)
        assertEquals(MONDAY_NOON, result.watch.resolvedThrough)
    }

    @Test
    fun `resolving never moves back`() {
        val watch = watch(resolvedThrough = "2026-11-16T12:00:00Z")

        assertEquals(watch, watch.resolve(Instant.parse("2026-11-09T12:00:00Z")))
        assertEquals(
            Instant.parse("2026-11-23T12:00:00Z"),
            watch.resolve(Instant.parse("2026-11-23T12:00:00Z")).resolvedThrough,
        )
    }

    // endregion

    @Test
    fun `slots older than a day or never fetched are stale`() {
        assertTrue(watch(airingCheckedAt = null).isAiringStale(MONDAY_NOON))
        assertTrue(watch(airingCheckedAt = "2026-11-15T12:00:00Z").isAiringStale(MONDAY_NOON))
        assertFalse(watch(airingCheckedAt = "2026-11-15T12:00:01Z").isAiringStale(MONDAY_NOON))
    }

    @Test
    fun `the update carries the slug and the tracking state`() {
        val watch = watch(lastSeen = seen("08", "2026-11-16T11:00:00Z"), resolvedThrough = "2026-11-16T12:00:00Z")

        assertEquals(ReleaseWatchUpdate(watch.show.slug, watch.lastSeen, MONDAY_NOON), watch.toUpdate())
    }
}
