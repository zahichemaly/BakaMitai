package com.zc.bakamitai.feature.home.domain.usecase

import com.zc.bakamitai.common.domain.util.TimeZoneProvider
import com.zc.bakamitai.feature.home.domain.model.Release
import com.zc.bakamitai.feature.home.domain.model.ScheduledShow
import com.zc.bakamitai.feature.home.domain.model.TodayBroadcasts
import com.zc.bakamitai.feature.home.domain.model.UpcomingBroadcast
import com.zc.bakamitai.feature.home.domain.repository.BroadcastRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import kotlin.time.Clock

/**
 * What Home shows: today's upcoming shows and today's releases, in the device's time zone
 * (docs/1-home.md §4.3).
 *
 * The schedule and the first latest page are required. The newest search results are merged in as well,
 * because `f=latest` can lag by days (api.md, latest releases); they are best effort, like the second latest
 * page and the episode-number lookups.
 */
@Factory
class GetTodayBroadcastsUseCase(
    private val repository: BroadcastRepository,
    private val findLatestEpisodeNumber: FindLatestEpisodeNumberUseCase,
    private val clock: Clock,
    private val timeZoneProvider: TimeZoneProvider,
) {

    suspend operator fun invoke(): Result<TodayBroadcasts> = coroutineScope {
        val timeZone = timeZoneProvider.current()
        val now = clock.now()
        val today = now.toLocalDateTime(timeZone).date

        val scheduleRequest = async { repository.getTodaySchedule(timeZone) }
        val firstPageRequest = async { repository.getLatestReleases(page = FIRST_PAGE, timeZone = timeZone) }
        val newestRequest = async { repository.searchReleases(query = NEWEST_QUERY, timeZone = timeZone) }

        val schedule = scheduleRequest.await()
        val firstPage = firstPageRequest.await()
        val newest = newestRequest.await().getOrDefault(emptyList())
        if (schedule.isFailure || firstPage.isFailure) {
            return@coroutineScope Result.failure(
                checkNotNull(schedule.exceptionOrNull() ?: firstPage.exceptionOrNull()),
            )
        }
        val latest = firstPage.getOrThrow()

        val secondPage = if (latest.isNotEmpty() && latest.all { it.dateIn(timeZone) == today }) {
            repository.getLatestReleases(page = SECOND_PAGE, timeZone = timeZone).getOrDefault(emptyList())
        } else {
            emptyList()
        }

        val known = (latest + newest + secondPage)
            .sortedByDescending { it.releasedAt }
            .distinctBy { it.show.slug to it.episode }
        val released = known
            .filter { it.dateIn(timeZone) == today }
            .sortedWith(compareBy({ it.releasedAt }, { it.show.title }, { it.episode }))
        val releasedSlugs = released.mapTo(mutableSetOf()) { it.show.slug }
        val upcoming = schedule.getOrThrow()
            .filter { it.aired != true && it.show.slug !in releasedSlugs }
            .sortedWith(compareBy({ it.time }, { it.show.title }))
            .map { scheduled -> async { scheduled.toUpcoming(known, timeZone) } }
            .awaitAll()

        Result.success(
            TodayBroadcasts(
                date = today,
                timeZone = timeZone,
                utcOffset = timeZone.offsetAt(now),
                upcoming = upcoming,
                released = released,
            ),
        )
    }

    private suspend fun ScheduledShow.toUpcoming(known: List<Release>, timeZone: TimeZone) = UpcomingBroadcast(
        show = show,
        time = time,
        episodeNumber = findLatestEpisodeNumber(show, known, timeZone)?.plus(1),
    )

    private fun Release.dateIn(timeZone: TimeZone): LocalDate = releasedAt.toLocalDateTime(timeZone).date

    private companion object {
        const val FIRST_PAGE = 1
        const val SECOND_PAGE = 2

        /** An empty search returns the newest releases, even while `f=latest` lags. */
        const val NEWEST_QUERY = ""
    }
}
