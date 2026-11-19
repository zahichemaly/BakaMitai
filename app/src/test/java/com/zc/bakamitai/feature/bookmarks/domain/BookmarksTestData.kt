package com.zc.bakamitai.feature.bookmarks.domain

import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.feature.bookmarks.domain.model.AiringSlot
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.model.EpisodeKey
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatch
import com.zc.bakamitai.feature.bookmarks.domain.model.SeenEpisode
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

/** Builders for the Bookmarks domain in tests. 2026-11-16 is a Monday; times are UTC unless stated. */
object BookmarksTestData {

    /** Monday 16 Nov 2026, 12:00 UTC: the occurrence of [mondayNoon] most tests await. */
    val MONDAY_NOON: Instant = Instant.parse("2026-11-16T12:00:00Z")

    /** A slot every Monday at 12:00 UTC. */
    fun mondayNoon(releaseTitle: String = "Frieren") = slot(DayOfWeek.MONDAY, LocalTime(12, 0), releaseTitle)

    fun slot(day: DayOfWeek, time: LocalTime, releaseTitle: String = "Frieren", timeZone: TimeZone = TimeZone.UTC) =
        AiringSlot(dayOfWeek = day, time = time, timeZone = timeZone, releaseTitle = releaseTitle)

    fun bookmark(slug: String, addedAt: String, title: String = slug, airing: AiringSlot? = null) =
        Bookmark(show = show(slug, title), addedAt = Instant.parse(addedAt), airing = airing)

    fun watch(
        slug: String = "frieren",
        trackingSince: String = "2026-11-01T00:00:00Z",
        airing: AiringSlot? = mondayNoon(),
        airingCheckedAt: String? = "2026-11-16T00:00:00Z",
        lastSeen: SeenEpisode? = null,
        resolvedThrough: String? = null,
        title: String = slug,
    ) = ReleaseWatch(
        show = show(slug, title),
        trackingSince = Instant.parse(trackingSince),
        airing = airing,
        airingCheckedAt = airingCheckedAt?.let(Instant::parse),
        lastSeen = lastSeen,
        resolvedThrough = resolvedThrough?.let(Instant::parse),
    )

    fun seen(episode: String, releasedAt: String) = SeenEpisode(EpisodeKey(episode), Instant.parse(releasedAt))
}
