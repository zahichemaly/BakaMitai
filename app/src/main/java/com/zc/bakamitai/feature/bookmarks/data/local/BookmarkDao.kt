package com.zc.bakamitai.feature.bookmarks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** The `bookmark` table. Multi-statement writes are `@Transaction`s, so a double tap can't race. */
@Dao
abstract class BookmarkDao {

    /** Every bookmark, newest first, re-emitted on every change. */
    @Query(
        """
        SELECT * FROM bookmark
        ORDER BY added_at DESC
        """,
    )
    abstract fun observeAll(): Flow<List<BookmarkEntity>>

    /** Every bookmark, newest first. */
    @Query(
        """
        SELECT * FROM bookmark
        ORDER BY added_at DESC
        """,
    )
    abstract suspend fun getAll(): List<BookmarkEntity>

    @Query(
        """
        SELECT * FROM bookmark
        WHERE slug = :slug
        """,
    )
    abstract suspend fun get(slug: String): BookmarkEntity?

    @Query(
        """
        SELECT COUNT(*) FROM bookmark
        """,
    )
    abstract suspend fun count(): Int

    /** Inserts [entity] unless its slug is saved already. Returns the row id, or -1 when it was ignored. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(entity: BookmarkEntity): Long

    @Query(
        """
        DELETE FROM bookmark
        WHERE slug = :slug
        """,
    )
    protected abstract suspend fun deleteRow(slug: String): Int

    /** Deletes [slug]'s bookmark and returns it, or `null` when it wasn't saved. */
    @Transaction
    open suspend fun removeAndGet(slug: String): BookmarkEntity? = get(slug)?.also { deleteRow(slug) }

    /** Removes [entity]'s slug when it's saved, otherwise inserts [entity]. Returns whether it's saved afterwards. */
    @Transaction
    open suspend fun toggle(entity: BookmarkEntity): Boolean {
        if (removeAndGet(entity.slug) != null) return false
        insert(entity)
        return true
    }

    /** Stores refreshed weekly slots, in one transaction. Rows of slugs no longer saved are ignored. */
    @Update(entity = BookmarkEntity::class)
    abstract suspend fun updateAirings(rows: List<BookmarkAiringRow>): Int

    /** Stores a release check's tracking state, in one transaction. Rows of slugs no longer saved are ignored. */
    @Update(entity = BookmarkEntity::class)
    abstract suspend fun updateTrackings(rows: List<BookmarkTrackingRow>): Int
}
