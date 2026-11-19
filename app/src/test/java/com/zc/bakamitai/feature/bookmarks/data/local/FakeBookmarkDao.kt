package com.zc.bakamitai.feature.bookmarks.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * The `bookmark` table in memory. Its `@Transaction` functions ([removeAndGet], [toggle]) are the real ones,
 * inherited from [BookmarkDao]. `SQLiteException` can't be built in JVM tests (android.jar stubs), so storage
 * failures are covered by the instrumented tests; [failure] throws anything else, e.g. a cancellation.
 */
class FakeBookmarkDao : BookmarkDao() {

    val rows = MutableStateFlow<Map<String, BookmarkEntity>>(emptyMap())
    var failure: Throwable? = null
    var trackingWrites = 0

    override fun observeAll(): Flow<List<BookmarkEntity>> = rows.map { it.values.sortedByDescending { e -> e.addedAt } }

    override suspend fun getAll(): List<BookmarkEntity> {
        failure?.let { throw it }
        return rows.value.values.sortedByDescending { it.addedAt }
    }

    override suspend fun get(slug: String): BookmarkEntity? = rows.value[slug]

    override suspend fun count(): Int = rows.value.size

    override suspend fun insert(entity: BookmarkEntity): Long {
        failure?.let { throw it }
        if (entity.slug in rows.value) return -1
        rows.update { it + (entity.slug to entity) }
        return rows.value.size.toLong()
    }

    override suspend fun deleteRow(slug: String): Int {
        val existed = slug in rows.value
        rows.update { it - slug }
        return if (existed) 1 else 0
    }

    override suspend fun updateAirings(rows: List<BookmarkAiringRow>): Int = rows.count { row ->
        val current = this.rows.value[row.slug] ?: return@count false
        this.rows.update {
            it + (
                row.slug to current.copy(
                    airingDay = row.airingDay,
                    airingMinute = row.airingMinute,
                    airingZone = row.airingZone,
                    airingTitle = row.airingTitle,
                    airingCheckedAt = row.airingCheckedAt,
                )
                )
        }
        true
    }

    override suspend fun updateTrackings(rows: List<BookmarkTrackingRow>): Int {
        trackingWrites++
        return rows.count { row ->
            val current = this.rows.value[row.slug] ?: return@count false
            this.rows.update {
                it + (
                    row.slug to current.copy(
                        lastSeenEpisode = row.lastSeenEpisode,
                        lastSeenReleasedAt = row.lastSeenReleasedAt,
                        resolvedThrough = row.resolvedThrough,
                    )
                    )
            }
            true
        }
    }
}
