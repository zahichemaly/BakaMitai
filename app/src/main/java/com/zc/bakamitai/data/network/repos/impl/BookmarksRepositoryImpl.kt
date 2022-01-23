package com.zc.bakamitai.data.network.repos.impl

import com.zc.bakamitai.data.network.repos.BookmarksRepository
import com.zc.bakamitai.data.room.daos.BookmarkDao
import com.zc.bakamitai.data.room.entities.Bookmark

class BookmarksRepositoryImpl(private val bookmarkDao: BookmarkDao) : BookmarksRepository {
    private val _bookmarks = mutableListOf<Bookmark>()

    override suspend fun getBookmarks(): List<Bookmark> {
        if (_bookmarks.isEmpty()) {
            _bookmarks.addAll(bookmarkDao.getAll())
        }
        return _bookmarks
    }

    override suspend fun getBookmark(page: String): Bookmark? {
        return _bookmarks.firstOrNull { it.page == page } ?: return bookmarkDao.getById(page)
    }

    override suspend fun isBookmarked(id: String): Boolean {
        return bookmarkDao.isExists(id)
    }

    override suspend fun addBookmark(bookmark: Bookmark) {
        bookmarkDao.insert(bookmark)
        _bookmarks.add(bookmark)
    }

    override suspend fun removeBookmark(id: String) {
        bookmarkDao.delete(id)
        val index = _bookmarks.indexOfFirst { it.id == id }
        if (index > -1 && _bookmarks.isNotEmpty()) _bookmarks.removeAt(index)
    }
}
