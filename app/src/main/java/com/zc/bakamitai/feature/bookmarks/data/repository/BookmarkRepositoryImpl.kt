package com.zc.bakamitai.feature.bookmarks.data.repository

import com.zc.bakamitai.common.domain.model.DomainError
import com.zc.bakamitai.feature.bookmarks.data.dto.BookmarkDTO
import com.zc.bakamitai.feature.bookmarks.data.local.BookmarksLocalDataSource
import com.zc.bakamitai.feature.bookmarks.data.mapper.toDTO
import com.zc.bakamitai.feature.bookmarks.data.mapper.toDomain
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import java.io.IOException

@Single
class BookmarkRepositoryImpl(private val local: BookmarksLocalDataSource) : BookmarkRepository {

    override fun observeBookmarks(): Flow<List<Bookmark>> =
        local.bookmarks.map { bookmarks -> bookmarks.mapNotNull(BookmarkDTO::toDomain) }

    /** A failed disk write is [DomainError.Unknown]: it isn't a network problem (docs/4-details.md Q7). */
    override suspend fun toggle(bookmark: Bookmark): Result<Boolean> = try {
        Result.success(local.toggle(bookmark.toDTO()))
    } catch (e: IOException) {
        Result.failure(DomainError.Unknown(e))
    }
}
