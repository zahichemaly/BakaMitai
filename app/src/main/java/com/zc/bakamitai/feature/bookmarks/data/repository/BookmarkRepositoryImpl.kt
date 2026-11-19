package com.zc.bakamitai.feature.bookmarks.data.repository

import android.database.sqlite.SQLiteException
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkDao
import com.zc.bakamitai.feature.bookmarks.data.mapper.toAiringRow
import com.zc.bakamitai.feature.bookmarks.data.mapper.toBookmark
import com.zc.bakamitai.feature.bookmarks.data.mapper.toEntity
import com.zc.bakamitai.feature.bookmarks.data.mapper.toReleaseWatch
import com.zc.bakamitai.feature.bookmarks.data.mapper.toTrackingRow
import com.zc.bakamitai.feature.bookmarks.domain.model.AiringSlot
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkToggle
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatch
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseWatchUpdate
import com.zc.bakamitai.feature.bookmarks.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.time.Instant

/**
 * The bookmarks in Room (docs/5-bookmarks.md §4.3). A disk failure (`SQLiteException`: a corrupt database, a full
 * disk) is [DomainError.Unknown]: it isn't a network problem (docs/4-details.md Q7).
 */
@Single
class BookmarkRepositoryImpl(private val dao: BookmarkDao) : BookmarkRepository {

    override fun observeBookmarks(): Flow<List<Bookmark>> = dao.observeAll()
        .map { rows -> rows.mapNotNull { it.toBookmark() } }
        .catch { e -> throw if (e is SQLiteException) DomainError.Unknown(e) else e }

    override suspend fun toggle(bookmark: Bookmark): Result<BookmarkToggle> = storageCall {
        val isBookmarked = dao.toggle(bookmark.toEntity(trackingSince = bookmark.addedAt))
        BookmarkToggle(isBookmarked = isBookmarked, isFirstBookmark = isBookmarked && dao.count() == 1)
    }

    override suspend fun remove(slug: ShowSlug): Result<Bookmark?> =
        storageCall { dao.removeAndGet(slug.value)?.toBookmark() }

    override suspend fun restore(bookmark: Bookmark, trackingSince: Instant): Result<Unit> =
        storageCall { dao.insert(bookmark.toEntity(trackingSince)) }.map { }

    override suspend fun getReleaseWatches(): Result<List<ReleaseWatch>> =
        storageCall { dao.getAll().mapNotNull { it.toReleaseWatch() } }

    override suspend fun updateAiringSlots(slots: Map<ShowSlug, AiringSlot?>, checkedAt: Instant): Result<Unit> =
        storageCall { dao.updateAirings(slots.map { (slug, slot) -> slot.toAiringRow(slug, checkedAt) }) }.map { }

    override suspend fun saveReleaseWatches(updates: List<ReleaseWatchUpdate>): Result<Unit> = storageCall {
        if (updates.isNotEmpty()) dao.updateTrackings(updates.map { it.toTrackingRow() })
    }.map { }

    /** Runs a DAO call; `SQLiteException` becomes [DomainError.Unknown]. Cancellation isn't caught. */
    private suspend fun <T> storageCall(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: SQLiteException) {
        Result.failure(DomainError.Unknown(e))
    }
}
