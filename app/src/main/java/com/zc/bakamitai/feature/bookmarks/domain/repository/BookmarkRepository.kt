package com.zc.bakamitai.feature.bookmarks.domain.repository

import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {

    /** Every saved show, once per slug, in the order it was added. */
    fun observeBookmarks(): Flow<List<Bookmark>>

    /**
     * Saves [bookmark] unless its slug is already saved, in which case it removes it. Returns whether the slug is
     * saved afterwards.
     */
    suspend fun toggle(bookmark: Bookmark): Result<Boolean>
}
