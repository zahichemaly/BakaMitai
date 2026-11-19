package com.zc.bakamitai.feature.bookmarks.data.repository

import com.zc.bakamitai.common.data.util.safeDataStoreCall
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarkSortLocalDataSource
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkSort
import com.zc.bakamitai.feature.bookmarks.domain.repository.BookmarkSortRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

/** The Bookmarks sort from the local DataStore; writes are wrapped in [safeDataStoreCall]. */
@Single
class BookmarkSortRepositoryImpl(private val localDataSource: BookmarkSortLocalDataSource) : BookmarkSortRepository {

    override fun observeSort(): Flow<BookmarkSort> = localDataSource.sort

    override suspend fun setSort(sort: BookmarkSort): Result<Unit> = safeDataStoreCall { localDataSource.setSort(sort) }
}
