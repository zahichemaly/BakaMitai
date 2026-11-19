package com.zc.bakamitai.feature.bookmarks.domain.repository

import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Bookmarks in memory. [toggle] adds or removes by slug like the real store, unless [toggleFailure] is set.
 *
 * @property toggled every bookmark passed to [toggle], in call order.
 */
class FakeBookmarkRepository : BookmarkRepository {

    val bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    var toggleFailure: Throwable? = null
    val toggled = mutableListOf<Bookmark>()

    override fun observeBookmarks(): Flow<List<Bookmark>> = bookmarks

    override suspend fun toggle(bookmark: Bookmark): Result<Boolean> {
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
        return Result.success(isSaved)
    }
}
