package com.zc.bakamitai.feature.bookmarks.domain.repository

import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.bookmarks.domain.model.AiringSlot
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkToggle
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatch
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatchUpdate
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/** The saved shows and their new-episode tracking, on the device. Failures are `DomainError.Unknown`. */
interface BookmarkRepository {

    /** Every saved show, newest first, re-emitted on every change. A read failure ends it with a `DomainError`. */
    fun observeBookmarks(): Flow<List<Bookmark>>

    /**
     * Saves [bookmark] (tracking new episodes from its `addedAt`) unless its slug is saved already, in which case
     * it removes it, in one atomic write.
     */
    suspend fun toggle(bookmark: Bookmark): Result<BookmarkToggle>

    /** Removes [slug]'s bookmark and returns it (for Undo), or `null` when it wasn't saved. */
    suspend fun remove(slug: ShowSlug): Result<Bookmark?>

    /** Saves [bookmark] again with its original date, tracking new episodes from [trackingSince]. */
    suspend fun restore(bookmark: Bookmark, trackingSince: Instant): Result<Unit>

    /** Every bookmark's tracking state, for a release check. */
    suspend fun getReleaseWatches(): Result<List<ReleaseWatch>>

    /** Stores each slug's weekly slot (`null` = not on the weekly schedule), fetched at [checkedAt]. */
    suspend fun updateAiringSlots(slots: Map<ShowSlug, AiringSlot?>, checkedAt: Instant): Result<Unit>

    /** Stores a release check's tracking state. Slugs no longer saved are ignored. */
    suspend fun saveReleaseWatches(updates: List<ReleaseWatchUpdate>): Result<Unit>
}
