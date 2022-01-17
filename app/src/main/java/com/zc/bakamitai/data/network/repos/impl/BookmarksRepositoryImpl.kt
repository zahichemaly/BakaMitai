package com.zc.bakamitai.data.network.repos.impl

import com.zc.bakamitai.data.network.repos.BookmarksRepository
import com.zc.bakamitai.data.room.daos.BookmarkDao
import com.zc.bakamitai.data.room.entities.Bookmark

class BookmarksRepositoryImpl(private val bookmarkDao: BookmarkDao) : BookmarksRepository {
    override suspend fun getBookmarks(): List<Bookmark> {
        return bookmarkDao.getAll()
    }

    override suspend fun getBookmark(page: String): Bookmark? {
        return bookmarkDao.getById(page)
    }

    override suspend fun isBookmarked(id: String): Boolean {
        return bookmarkDao.isExists(id)
    }

    override suspend fun addBookmark(bookmark: Bookmark) {
        bookmarkDao.insert(bookmark)
    }

    override suspend fun removeBookmark(id: String) {
        bookmarkDao.delete(id)
    }
}
