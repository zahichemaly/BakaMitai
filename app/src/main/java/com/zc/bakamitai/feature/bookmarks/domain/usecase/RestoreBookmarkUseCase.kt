package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.feature.bookmarks.domain.model.Bookmark
import com.zc.bakamitai.feature.bookmarks.domain.repository.BookmarkRepository
import com.zc.bakamitai.feature.bookmarks.domain.repository.ReleaseCheckScheduler
import org.koin.core.annotation.Factory
import kotlin.time.Clock

/**
 * Undoes a removal: saves the bookmark again with its original date (its place in "Recently added"), tracking new
 * episodes from now, so nothing released while it was removed notifies.
 */
@Factory
class RestoreBookmarkUseCase(
    private val repository: BookmarkRepository,
    private val scheduler: ReleaseCheckScheduler,
    private val clock: Clock,
) {

    suspend operator fun invoke(bookmark: Bookmark): Result<Unit> =
        repository.restore(bookmark, trackingSince = clock.now()).onSuccess { scheduler.requestCheck() }
}
