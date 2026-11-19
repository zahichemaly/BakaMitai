package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.util.TimeZoneProvider
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkSort
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkedShow
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseCheckPolicy.LABEL_REFRESH
import com.zc.bakamitai.feature.bookmarks.domain.repository.BookmarkRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * The bookmarks in [BookmarkSort] order, each with its next airing in the device's time zone. Re-derived on every
 * change and every minute, so "TODAY 9:30 PM" moves on once the slot has passed; equal lists aren't re-emitted.
 */
@Factory
class ObserveBookmarksUseCase(
    private val repository: BookmarkRepository,
    private val clock: Clock,
    private val timeZoneProvider: TimeZoneProvider,
) {

    operator fun invoke(sort: BookmarkSort): Flow<List<BookmarkedShow>> =
        combine(repository.observeBookmarks(), ticker()) { bookmarks, _ -> bookmarks.toBookmarkedShows(sort) }
            .distinctUntilChanged()

    private fun ticker(): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(LABEL_REFRESH)
        }
    }

    private fun List<Bookmark>.toBookmarkedShows(sort: BookmarkSort): List<BookmarkedShow> {
        val now = clock.now()
        val timeZone = timeZoneProvider.current()
        val today = now.toLocalDateTime(timeZone).date
        return map { bookmark -> NextAiring(bookmark, bookmark.airing?.firstAfter(now)) }
            .sortedWith(sort.comparator())
            .map { (bookmark, next) ->
                val local = next?.toLocalDateTime(timeZone)
                BookmarkedShow(
                    bookmark = bookmark,
                    addedOn = bookmark.addedAt.toLocalDateTime(timeZone).date,
                    nextAiring = local,
                    airsToday = local?.date == today,
                )
            }
    }

    private fun BookmarkSort.comparator(): Comparator<NextAiring> {
        val byTitle = compareBy<NextAiring>({ it.bookmark.show.title.lowercase() }, { it.bookmark.show.slug.value })
        return when (this) {
            BookmarkSort.RECENTLY_ADDED ->
                compareByDescending<NextAiring> { it.bookmark.addedAt }.thenBy { it.bookmark.show.slug.value }

            BookmarkSort.TITLE -> byTitle

            BookmarkSort.NEXT_AIRING -> compareBy<NextAiring, Instant?>(nullsLast()) { it.instant }.then(byTitle)
        }
    }

    private data class NextAiring(val bookmark: Bookmark, val instant: Instant?)
}
