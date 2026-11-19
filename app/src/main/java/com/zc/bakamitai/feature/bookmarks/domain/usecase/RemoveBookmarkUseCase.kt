package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.repository.BookmarkRepository
import com.zc.bakamitai.feature.bookmarks.domain.repository.ReleaseCheckScheduler
import org.koin.core.annotation.Factory

/** Removes a bookmark and returns it, for Undo (`null` when it wasn't saved); reschedules the release checks. */
@Factory
class RemoveBookmarkUseCase(private val repository: BookmarkRepository, private val scheduler: ReleaseCheckScheduler) {

    suspend operator fun invoke(slug: ShowSlug): Result<Bookmark?> =
        repository.remove(slug).onSuccess { removed -> if (removed != null) scheduler.requestCheck() }
}
