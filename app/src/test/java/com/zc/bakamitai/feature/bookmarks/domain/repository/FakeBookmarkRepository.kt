package com.zc.bakamitai.feature.bookmarks.domain.repository

import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.bookmarks.domain.model.AiringSlot
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkToggle
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatch
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatchUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlin.time.Instant

/**
 * Bookmarks and their release tracking in memory. [bookmarks] (the screen's data) and [watches] (the release
 * checks' data) are separate lists: tests set up the one they need. Every write fails with its `…Failure` when set.
 *
 * @property observeFailure when set, collecting [observeBookmarks] fails with it (checked on every collection).
 */
class FakeBookmarkRepository : BookmarkRepository {

    val bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    var watches: List<ReleaseWatch> = emptyList()

    var observeFailure: Throwable? = null
    var toggleFailure: Throwable? = null
    var removeFailure: Throwable? = null
    var restoreFailure: Throwable? = null
    var watchesFailure: Throwable? = null
    var airingFailure: Throwable? = null
    var saveFailure: Throwable? = null

    val toggled = mutableListOf<Bookmark>()
    val restored = mutableListOf<Pair<Bookmark, Instant>>()
    val airingUpdates = mutableListOf<Pair<Map<ShowSlug, AiringSlot?>, Instant>>()
    val savedUpdates = mutableListOf<List<ReleaseWatchUpdate>>()
    var watchReads = 0

    override fun observeBookmarks(): Flow<List<Bookmark>> = flow {
        observeFailure?.let { throw it }
        emitAll(bookmarks)
    }

    override suspend fun toggle(bookmark: Bookmark): Result<BookmarkToggle> {
        toggled += bookmark
        toggleFailure?.let { return Result.failure(it) }
        var isSaved = false
        bookmarks.update { current ->
            if (current.any { it.show.slug == bookmark.show.slug }) {
                current.filterNot { it.show.slug == bookmark.show.slug }
            } else {
                isSaved = true
                current + bookmark
            }
        }
        return Result.success(BookmarkToggle(isSaved, isFirstBookmark = isSaved && bookmarks.value.size == 1))
    }

    override suspend fun remove(slug: ShowSlug): Result<Bookmark?> {
        removeFailure?.let { return Result.failure(it) }
        val removed = bookmarks.value.firstOrNull { it.show.slug == slug }
        bookmarks.update { current -> current.filterNot { it.show.slug == slug } }
        return Result.success(removed)
    }

    override suspend fun restore(bookmark: Bookmark, trackingSince: Instant): Result<Unit> {
        restored += bookmark to trackingSince
        restoreFailure?.let { return Result.failure(it) }
        bookmarks.update { current ->
            if (current.any { it.show.slug == bookmark.show.slug }) current else current + bookmark
        }
        return Result.success(Unit)
    }

    override suspend fun getReleaseWatches(): Result<List<ReleaseWatch>> {
        watchReads++
        watchesFailure?.let { return Result.failure(it) }
        return Result.success(watches)
    }

    override suspend fun updateAiringSlots(slots: Map<ShowSlug, AiringSlot?>, checkedAt: Instant): Result<Unit> {
        airingUpdates += slots to checkedAt
        airingFailure?.let { return Result.failure(it) }
        watches = watches.map { watch ->
            if (watch.show.slug in slots) {
                watch.copy(airing = slots[watch.show.slug], airingCheckedAt = checkedAt)
            } else {
                watch
            }
        }
        return Result.success(Unit)
    }

    override suspend fun saveReleaseWatches(updates: List<ReleaseWatchUpdate>): Result<Unit> {
        savedUpdates += updates
        saveFailure?.let { return Result.failure(it) }
        val bySlug = updates.associateBy { it.slug }
        watches = watches.map { watch ->
            bySlug[watch.show.slug]?.let { watch.copy(lastSeen = it.lastSeen, resolvedThrough = it.resolvedThrough) }
                ?: watch
        }
        return Result.success(Unit)
    }
}
