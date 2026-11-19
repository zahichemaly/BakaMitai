package com.zc.bakamitai.feature.bookmarks.data.repository

import android.database.sqlite.SQLiteFullException
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkAiringRow
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkDao
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkEntity
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkTrackingRow
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.time.Instant

/** Disk failures need Android's `SQLiteException`, so they're tested here rather than in the JVM tests. */
@RunWith(AndroidJUnit4::class)
class BookmarkRepositoryImplStorageErrorTest {

    private val repository = BookmarkRepositoryImpl(FullDiskDao())
    private val bookmark = Bookmark(Show(ShowSlug("frieren"), "Frieren", null), Instant.parse("2026-11-19T08:00:00Z"))

    @Test
    fun everyWriteAndReadFailsWithUnknown() = runTest {
        assertIs<DomainError.Unknown>(repository.toggle(bookmark).exceptionOrNull())
        assertIs<DomainError.Unknown>(repository.remove(ShowSlug("frieren")).exceptionOrNull())
        assertIs<DomainError.Unknown>(repository.restore(bookmark, bookmark.addedAt).exceptionOrNull())
        assertIs<DomainError.Unknown>(repository.getReleaseWatches().exceptionOrNull())
        assertIs<DomainError.Unknown>(repository.updateAiringSlots(emptyMap(), bookmark.addedAt).exceptionOrNull())
    }

    @Test
    fun aFailedObservationEndsWithUnknown() = runTest {
        assertFailsWith<DomainError.Unknown> { repository.observeBookmarks().first() }
    }

    private class FullDiskDao : BookmarkDao() {
        override fun observeAll(): Flow<List<BookmarkEntity>> = flow { throw SQLiteFullException("full") }

        override suspend fun getAll(): List<BookmarkEntity> = throw SQLiteFullException("full")

        override suspend fun get(slug: String): BookmarkEntity = throw SQLiteFullException("full")

        override suspend fun count(): Int = throw SQLiteFullException("full")

        override suspend fun insert(entity: BookmarkEntity): Long = throw SQLiteFullException("full")

        override suspend fun deleteRow(slug: String): Int = throw SQLiteFullException("full")

        override suspend fun updateAirings(rows: List<BookmarkAiringRow>): Int = throw SQLiteFullException("full")

        override suspend fun updateTrackings(rows: List<BookmarkTrackingRow>): Int = throw SQLiteFullException("full")
    }
}
