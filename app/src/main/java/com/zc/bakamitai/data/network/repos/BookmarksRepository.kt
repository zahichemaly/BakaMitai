package com.zc.bakamitai.data.network.repos

import com.zc.bakamitai.data.room.entities.Bookmark

interface BookmarksRepository {
    suspend fun getBookmarks(): List<Bookmark>
    suspend fun getBookmark(page: String): Bookmark?
    suspend fun isBookmarked(id: String): Boolean
    suspend fun addBookmark(bookmark: Bookmark)
    suspend fun removeBookmark(id: String)
}
